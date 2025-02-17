package customer.api;

import akka.NotUsed;
import akka.grpc.GrpcServiceException;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.GrpcEndpoint;
import akka.javasdk.client.ComponentClient;
import akka.stream.javadsl.Source;
import customer.api.proto.*;
import customer.application.CustomerByEmailView;
import customer.application.CustomerByNameView;
import customer.application.CustomerEntity;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
// tag::class[]
// tag::endpoint-component-interaction[]
@GrpcEndpoint // <1>
public class CustomerGrpcEndpointImpl implements CustomerGrpcEndpoint {
  // tag::class[]

  private static final Logger log = LoggerFactory.getLogger(CustomerGrpcEndpointImpl.class);

  private final ComponentClient componentClient;

  public CustomerGrpcEndpointImpl(ComponentClient componentClient) { // <2>
    this.componentClient = componentClient;
  }
  // end::endpoint-component-interaction[]

  @Override
  public CompletionStage<CreateCustomerResponse> createCustomer(CreateCustomerRequest in) {
    log.info("gRPC request to create customer: {}", in);
    if (in.getCustomerId().isBlank())
      throw new IllegalArgumentException("Customer id must not be empty");

    return componentClient.forEventSourcedEntity(in.getCustomerId())
        .method(CustomerEntity::create)
        .invokeAsync(apiToDomain(in.getCustomer()))
        .thenApply(__ -> CreateCustomerResponse.getDefaultInstance());
  }

  // tag::get[]

  @Override
  public CompletionStage<Customer> getCustomer(GetCustomerRequest in) {
    // tag::exception[]
    if (in.getCustomerId().isBlank())
      throw new GrpcServiceException(
          Status.INVALID_ARGUMENT.augmentDescription("Customer id must not be empty"));
    // end::exception[]

    return componentClient.forEventSourcedEntity(in.getCustomerId()) // <3>
        .method(CustomerEntity::getCustomer)
        .invokeAsync()
        .thenApply(this::domainToApi) // <4>
        .exceptionally(ex -> {
          if (ex.getMessage().contains("No customer found for id")) throw new GrpcServiceException(Status.NOT_FOUND);
          else throw new RuntimeException(ex);
        });
  }
  // end::get[]


  @Override
  public CompletionStage<ChangeNameResponse> changeName(ChangeNameRequest in) {
    log.info("gRPC request to change customer [{}] name: {}", in.getCustomerId(), in.getNewName());
    return componentClient.forEventSourcedEntity(in.getCustomerId())
        .method(CustomerEntity::changeName)
        .invokeAsync(in.getNewName())
        .thenApply(__ -> ChangeNameResponse.getDefaultInstance());
  }

  @Acl(deny = @Acl.Matcher(principal = Acl.Principal.ALL))
  @Override
  public CompletionStage<ChangeAddressResponse> changeAddress(ChangeAddressRequest in) {
    log.info("gRPC request to change customer [{}] address: {}", in.getCustomerId(), in.getNewAddress());
    return componentClient.forEventSourcedEntity(in.getCustomerId())
        .method(CustomerEntity::changeAddress)
        .invokeAsync(apiToDomain(in.getNewAddress()))
        .thenApply(__ -> ChangeAddressResponse.getDefaultInstance());
  }

  // The two methods below are not necessarily realistic since we have the full result in one response,
  // but provides examples of streaming a response
  @Override
  public CompletionStage<CustomerList> customerByName(CustomerByNameRequest in) {
    return componentClient.forView()
        .method(CustomerByNameView::getCustomers)
        .invokeAsync(in.getName())
        .thenApply(viewCustomerList -> {
          var apiCustomers = viewCustomerList.customers().stream().map(this::domainToApi).toList();

          return CustomerList.newBuilder().addAllCustomers(apiCustomers).build();
        });
  }

  @Override
  public CompletionStage<CustomerList> customerByEmail(CustomerByEmailRequest in) {
    return componentClient.forView()
        .method(CustomerByEmailView::getCustomers)
        .invokeAsync(in.getEmail())
        .thenApply(viewCustomerList -> {
          var apiCustomers = viewCustomerList.customers().stream().map(this::domainToApi).toList();

          return CustomerList.newBuilder().addAllCustomers(apiCustomers).build();
        });
  }

  // tag::customerByEmailStream[]
  @Override
  public Source<CustomerSummary, NotUsed> customerByEmailStream(CustomerByEmailRequest in) {
    // Shows of streaming consumption of a view, transforming
    // each element and passing along to a streamed response
    var customerSummarySource = componentClient.forView()
        .stream(CustomerByEmailView::getCustomersStream)
        .source(in.getEmail());

    return customerSummarySource.map(c ->
      CustomerSummary.newBuilder()
          .setName(c.name())
          .setEmail(c.email())
          .build());
  }
  // end::customerByEmailStream[]

  // Conversions between the public gRPC API protobuf messages and the internal
  // Java domain classes.
  private customer.domain.Customer apiToDomain(Customer protoCustomer) {
    return new customer.domain.Customer(
        protoCustomer.getEmail(),
        protoCustomer.getName(),
        apiToDomain(protoCustomer.getAddress())
    );
  }

  private customer.domain.Address apiToDomain(Address protoAddress) {
    if (protoAddress == null) return null;
    else {
      return new customer.domain.Address(
          protoAddress.getStreet(),
          protoAddress.getCity()
      );
    }
  }

  // tag::endpoint-component-interaction[]

  private Customer domainToApi(customer.domain.Customer domainCustomer) {
    return Customer.newBuilder()
        .setName(domainCustomer.name())
        .setEmail(domainCustomer.email())
        .setAddress(domainToApi(domainCustomer.address()))
        .build();
  }

  private Address domainToApi(customer.domain.Address domainAddress) {
    if (domainAddress == null) return null;
    else {
      return Address.newBuilder()
          .setCity(domainAddress.city())
          .setStreet(domainAddress.street())
          .build();
    }
  }
  // end::endpoint-component-interaction[]

  private Customer domainToApi(customer.domain.CustomerRow domainRow) {
    return Customer.newBuilder()
        .setName(domainRow.name())
        .setEmail(domainRow.email())
        .setAddress(domainToApi(domainRow.address()))
        .build();
  }
}
