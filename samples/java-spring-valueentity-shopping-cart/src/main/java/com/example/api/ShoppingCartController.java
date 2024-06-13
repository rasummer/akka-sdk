package com.example.api;

import com.example.api.ShoppingCartDTO.LineItemDTO;
import kalix.javasdk.Metadata;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.ForwardHeaders;
import kalix.javasdk.client.ComponentClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

// tag::forward[]
// tag::forward-headers[]

@RequestMapping("/carts")
// end::forward[]
@ForwardHeaders("UserRole") // <1>
// tag::forward[]
public class ShoppingCartController extends Action {
  // end::forward-headers[]

  private final ComponentClient componentClient;

  public ShoppingCartController(ComponentClient componentClient) {
    this.componentClient = componentClient; // <1>
  }

  // end::forward[]

  // tag::initialize[]
  @PostMapping("/create")
  public Effect<String> initializeCart() {
    final String cartId = UUID.randomUUID().toString(); // <1>
    CompletionStage<ShoppingCartDTO> shoppingCartCreated =
      componentClient.forValueEntity(cartId)
        .method(ShoppingCartEntity::create) // <2>
        .invokeAsync(); // <3>


    // transform response
    CompletionStage<Effect<String>> effect =
      shoppingCartCreated.handle((empty, error) -> { // <4>
        if (error == null) {
          return effects().reply(cartId); // <5>
        } else {
          return effects().error("Failed to create cart, please retry"); // <6>
        }
      });

    return effects().asyncEffect(effect); // <7>
  }
  // end::initialize[]

  // tag::forward[]
  @PostMapping("/{cartId}/items/add") // <2>
  public Action.Effect<ShoppingCartDTO> verifiedAddItem(@PathVariable String cartId,
                                                        @RequestBody LineItemDTO addLineItem) {
    if (addLineItem.name().equalsIgnoreCase("carrot")) { // <3>
      return effects().error("Carrots no longer for sale"); // <4>
    } else {
      var addItemResult = componentClient.forValueEntity(cartId)
        .method(ShoppingCartEntity::addItem)
        .invokeAsync(addLineItem); // <5>
      return effects().asyncReply(addItemResult); // <6> FIXME no longer forward as documented
    }
  }
  // end::forward[]


  // tag::createPrePopulated[]
  @PostMapping("/prepopulated")
  public Action.Effect<String> createPrePopulated() {
    final String cartId = UUID.randomUUID().toString();
    CompletionStage<ShoppingCartDTO> shoppingCartCreated =
      componentClient.forValueEntity(cartId).method(ShoppingCartEntity::create).invokeAsync();

    CompletionStage<ShoppingCartDTO> cartPopulated =
      shoppingCartCreated.thenCompose(empty -> { // <1>
        var initialItem = new LineItemDTO("e", "eggplant", 1);

        return componentClient.forValueEntity(cartId)
          .method(ShoppingCartEntity::addItem)
          .invokeAsync(initialItem); // <2>
      });

    CompletionStage<String> reply = cartPopulated.thenApply(ShoppingCartDTO::cartId); // <4>

    return effects()
      .asyncReply(reply); // <5>
  }
  // end::createPrePopulated[]

  // tag::unsafeValidation[]
  @PostMapping("/{cartId}/unsafeAddItem")
  public Action.Effect<String> unsafeValidation(@PathVariable String cartId,
                                                @RequestBody LineItemDTO addLineItem) {
    // NOTE: This is an example of an anti-pattern, do not copy this
    CompletionStage<ShoppingCartDTO> cartReply =
      componentClient.forValueEntity(cartId).method(ShoppingCartEntity::getCart).invokeAsync(); // <1>

    CompletionStage<Action.Effect<String>> effect = cartReply.thenApply(cart -> {
      int totalCount = cart.items().stream()
        .mapToInt(LineItemDTO::quantity)
        .sum();

      if (totalCount < 10) {
        return effects().error("Max 10 items in a cart");
      } else {
        CompletionStage<String> addItemReply =
          componentClient.forValueEntity(cartId)
            .method(ShoppingCartEntity::addItem).invokeAsync(addLineItem)
            .thenApply(ShoppingCartDTO::cartId);
        return effects()
          .asyncReply(addItemReply); // <2>
      }
    });

    return effects().asyncEffect(effect);
  }
  // end::unsafeValidation[]

  // tag::forward-headers[]

  @DeleteMapping("/{cartId}")
  public Effect<String> removeCart(@PathVariable String cartId,
                                   @RequestHeader("UserRole") String userRole) { // <2>

    var userRoleFromMeta = actionContext().metadata().get("UserRole").get(); // <3>

    Metadata metadata = Metadata.EMPTY.add("Role", userRole);
    return effects().asyncReply(
      componentClient.forValueEntity(cartId)
        .method(ShoppingCartEntity::removeCart)
        .withMetadata(metadata)
        // FIXME no longer forward as documented
        .invokeAsync()); // <4>
  }
}
// end::forward-headers[]
