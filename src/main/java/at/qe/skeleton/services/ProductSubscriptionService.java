package at.qe.skeleton.services;

import at.qe.skeleton.events.ProductEvent;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.ProductEventType;
import at.qe.skeleton.model.ProductSubscription;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.repositories.ProductSubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class ProductSubscriptionService {
    private final ProductSubscriptionRepository productSubscriptionRepository;
    private final ProductService productService;
    private final NotificationService notificationService;
    private final UserxService userxService;

    public ProductSubscriptionService(ProductSubscriptionRepository productSubscriptionRepository, NotificationService notificationService, ProductService productService, UserxService userxService) {
        this.productSubscriptionRepository = productSubscriptionRepository;
        this.productService = productService;
        this.notificationService = notificationService;
        this.userxService = userxService;
    }

    @EventListener
    public void notifySubscribers(ProductEvent productEvent) {
        Collection<ProductSubscription> interestedUsers = productSubscriptionRepository.findAllByProductIdAndNotifyOnContaining(productEvent.productId(), productEvent.type());
        for (ProductSubscription interestedUser : interestedUsers) {
            Userx user = userxService.loadUserProtected(interestedUser.getUser().getId()).orElseThrow(EntityNotFoundException::new);
            String message = buildProductEventMessage(productEvent, user);
            notificationService.notify(message, user);
        }
    }

    private String buildProductEventMessage(ProductEvent productEvent, Userx user) {
        String message = "Hallo " + user.getFirstName() + " " + user.getLastName()+",\n";
        Product target = productService.loadProduct(productEvent.productId()).orElseThrow(EntityNotFoundException::new);
        switch (productEvent.type()) {
            case FOR_SALE:
                message += "Das Produkt: "+ target.getName() + " ist jetzt " + target.getDiscount() * 100 + "% reduziert!";
                break;
            case BACK_IN_STOCK:
                message += "Das Produkt: " + target.getName() + " ist jetzt wieder verfügbar!";
                break;
        }
        message += "\n\nMit freundlichen Grüßen\nIhr Webshop Team";
        return message;
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    public void createProductSubscription(Userx user, Long productId, ProductEventType type) {
        validateArgs(user, productId, type);
        Product product = productService.loadProduct(productId).orElseThrow(EntityNotFoundException::new);
        ProductSubscription productSubscription = new ProductSubscription();
        productSubscription.setUser(user);
        productSubscription.setProduct(product);
        productSubscription.addNotifyEvent(type);
        productSubscriptionRepository.save(productSubscription);
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    public void addProductSubscription(Userx user, Long productId, ProductEventType type) {
        validateArgs(user, productId, type);
        Optional<ProductSubscription> productSubscription = productSubscriptionRepository.findByProductIdAndUser(productId, user);
        if (productSubscription.isEmpty()) {
            createProductSubscription(user, productId, type);
        } else {
            productSubscription.get().addNotifyEvent(type);
            productSubscriptionRepository.save(productSubscription.get());
        }
    }


    @PreAuthorize("hasAuthority('CUSTOMER')")
    public void removeProductSubscriptionEvent(Userx user, Long productId, ProductEventType type) {
        validateArgs(user, productId, type);
        ProductSubscription productSubscription = productSubscriptionRepository.findByProductIdAndUser(productId, user).orElseThrow(EntityNotFoundException::new);
        productSubscription.removeNotifyEvent(type);
        if (productSubscription.getNotifyOn().isEmpty()) {
            productSubscriptionRepository.delete(productSubscription);
            return;
        }
        productSubscriptionRepository.save(productSubscription);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public void deleteProductSubscription(Userx user, Long productId) {
        validateArgs(user, productId);
        ProductSubscription productSubscription = productSubscriptionRepository.findByProductIdAndUser(productId, user).orElseThrow(EntityNotFoundException::new);
        productSubscriptionRepository.delete(productSubscription);
    }

    private void validateArgs(Userx user, Long productId) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("ProductId is null");
        }
    }

    private void validateArgs(Userx user, Long productId, ProductEventType type) {
        validateArgs(user, productId);
        if (type == null) {
            throw new IllegalArgumentException("Type is null");
        }
    }
}
