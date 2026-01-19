package at.qe.skeleton.tests;

import at.qe.skeleton.configs.JwtConfig;
import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.configs.TokenAuthenticationFilter;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.dtos.ProductFilterDTO;
import at.qe.skeleton.dtos.ReviewDTO;
import at.qe.skeleton.mappers.ProductMapper;
import at.qe.skeleton.mappers.ReviewMapper;
import at.qe.skeleton.model.*;
import at.qe.skeleton.repositories.ProductSubscriptionRepository;
import at.qe.skeleton.services.ProductService;
import at.qe.skeleton.services.ProductSubscriptionService;
import at.qe.skeleton.services.UserxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoSpyBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private ReviewMapper reviewMapper;

    @MockitoBean
    private UserxService userxService;

    @MockitoBean
    private ProductSubscriptionService productSubscriptionService;
    @MockitoBean
    private ProductSubscriptionRepository productSubscriptionRepository;

    @BeforeEach
    void setUp() throws Exception {
        // mock setup of the Authentication Filter
        Mockito.doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(tokenAuthenticationFilter).doFilterInternal(
                Mockito.any(HttpServletRequest.class),
                Mockito.any(HttpServletResponse.class),
                Mockito.any(FilterChain.class)
        );

        @SuppressWarnings("unchecked")
        Jws<Claims> mockJws = (Jws<Claims>) Mockito.mock(Jws.class);
        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.getSubject()).thenReturn("customer");
        Mockito.when(mockJws.getPayload()).thenReturn(mockClaims);
        Mockito.when(jwtTokenProvider.validateTokenAndGetJws(Mockito.anyString()))
               .thenReturn(Optional.of(mockJws));
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetProductProductExists() throws Exception {
        Long id = 1L;
        String name = "Product 1";
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(100.0);
        product.setStock(10);

        Mockito.when(productService.loadProduct(id)).thenReturn(Optional.of(product));
        Mockito.when(productMapper.mapTo(Mockito.eq(product), Mockito.anyMap())).thenReturn(new ProductDTO(id, name, 100.0, 10, 0.0, 0.0, null, null, null, null, null, null, null));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/{id}", id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name));
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetProductDoesNotExist() throws Exception {
        Mockito.when(productService.loadProduct(1L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetProducts() throws Exception {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");

        List<Product> products = List.of(product1, product2); // product 3 has to be left out here as the PageImpl does not remove it in this case
        Pageable pageable = PageRequest.of(0, 2);
        int total_count = 3;

        Page<Product> page = new PageImpl<>(products, pageable, total_count);

        Mockito.when(productService.getProducts(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(2),
                ArgumentMatchers.any(Sort.class),
                ArgumentMatchers.any(ProductFilterDTO.class)
        )).thenReturn(page);

        Mockito.when(productMapper.mapTo(ArgumentMatchers.any(Product.class), ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    return new ProductDTO(product.getId(), product.getName(), 0.0, 0, 0.0, 0.0, null, null, null, null, null, null, null);
                });

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/products")
                                              .param("pageId", "0")
                                              .param("pageSize", "2"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(2))
               .andExpect(MockMvcResultMatchers.jsonPath("$.pageIdAfter").value(1))
               .andExpect(MockMvcResultMatchers.jsonPath("$.items", Matchers.hasSize(2)))
               .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Product 1"))
               .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].name").value("Product 2"))
               .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value(3))
               .andExpect(MockMvcResultMatchers.jsonPath("$.pageCount").value(2));
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetProductsWithFilteringAndSorting() throws Exception {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());

        Mockito.when(productService.getProducts(ArgumentMatchers.any(), ArgumentMatchers.any(),
                                                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(emptyPage);

        // GET /api/product/products?name=Apple&minPrice=10.5&sort=price,desc&pageId=0
        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/products")
                                .param("name", "Apple")
                                .param("minPrice", "10.5")
                                .param("maxPrice", "20.5")
                                .param("minStock", "5")
                                .param("sort", "price,desc")
                                .param("pageId", "0")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk());

        // capture the arguments that were passed to the service
        ArgumentCaptor<ProductFilterDTO> filterCaptor = ArgumentCaptor.forClass(ProductFilterDTO.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        ArgumentCaptor<Integer> pageIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(productService).getProducts(
                pageIdCaptor.capture(),
                pageSizeCaptor.capture(), // we can skip verifying pageSize if we want, or capture it too
                sortCaptor.capture(),
                filterCaptor.capture()
        );

        // Check Filter Mapping
        ProductFilterDTO capturedFilter = filterCaptor.getValue();
        Assertions.assertEquals("Apple", capturedFilter.name());
        Assertions.assertEquals(10.5, capturedFilter.minPrice());
        Assertions.assertEquals(20.5, capturedFilter.maxPrice());
        Assertions.assertEquals(5, capturedFilter.minStock());

        // Check Sorting Mapping
        Sort capturedSort = sortCaptor.getValue();
        Assertions.assertEquals(Sort.Direction.DESC, Objects.requireNonNull(
                capturedSort.getOrderFor("price")).getDirection());

        // Check Pagination
        Assertions.assertEquals(0, pageIdCaptor.getValue());
        Assertions.assertEquals(10, pageSizeCaptor.getValue());
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetReviews() throws Exception {
        Review review1 = new Review();
        review1.setId(1L);
        review1.setRating(1);
        review1.setTitle("Review 1");
        review1.setComment("Review 1 comment");

        Review review2 = new Review();
        review2.setId(1L);
        review2.setRating(2);
        review2.setTitle("Review 2");
        review2.setComment("Review 2 comment");

        List<Review> reviews = List.of(review1, review2);
        Pageable pageable = PageRequest.of(0, 2);
        int total_count = 3;

        Page<Review> page = new PageImpl<>(reviews, pageable, total_count);

        Mockito.when(productService.getReviews(
                ArgumentMatchers.eq(1L),
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(2),
                ArgumentMatchers.any(Sort.class)
        )).thenReturn(page);

        Mockito.when(reviewMapper.mapTo(ArgumentMatchers.any(Review.class)))
                .thenAnswer(invocation -> {
                    Review review = invocation.getArgument(0);
                    return new ReviewDTO(review.getId(), null , null, review.getRating(), review.getTitle(), review.getComment(), null);
                });

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/{id}/reviews", 1L)
                                              .param("pageId", "0")
                                              .param("pageSize", "2")
                                              .param("sort", "rating,desc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageIdAfter").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value(total_count))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageCount").value(2));

        ArgumentCaptor<Long> productIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> pageIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        Mockito.verify(productService).getReviews(
                productIdCaptor.capture(),
                pageIdCaptor.capture(),
                pageSizeCaptor.capture(), // we can skip verifying pageSize if we want, or capture it too
                sortCaptor.capture()
        );

        // Check Sorting Mapping
        Sort capturedSort = sortCaptor.getValue();
        Assertions.assertEquals(Sort.Direction.DESC, Objects.requireNonNull(
                capturedSort.getOrderFor("rating")).getDirection());

        // Check Pagination
        Assertions.assertEquals(1L, productIdCaptor.getValue());
        Assertions.assertEquals(0, pageIdCaptor.getValue());
        Assertions.assertEquals(2, pageSizeCaptor.getValue());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"CUSTOMER"})
    public void testCreateReview() throws Exception {
        ReviewDTO reviewCreationRequest = new ReviewDTO(null, null, null, 4, "Review 1",
                                                        "Comment 1", null);
        Review review = new Review();
        review.setRating(4);
        review.setTitle("Review 1");
        review.setComment("Comment 1");

        Product product = new Product();
        product.setId(1L);

        Mockito.when(reviewMapper.mapFrom(ArgumentMatchers.any(ReviewDTO.class))).thenReturn(review);
        Mockito.when(productService.addReview(
                ArgumentMatchers.any(Long.class),
                ArgumentMatchers.any(Review.class),
                ArgumentMatchers.any()
        )).thenReturn(Optional.of(product));
        Mockito.when(productMapper.mapTo(ArgumentMatchers.any(Product.class))).thenReturn(
                new ProductDTO(1L, null, 2.0, 10, 0.0, 0.0, null, null, null, null, null, null, null));

        String jsonBody = new ObjectMapper().writeValueAsString(reviewCreationRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product/{id}/createReview", 1L)
                                              .with(SecurityMockMvcRequestPostProcessors.csrf())
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(jsonBody))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testDeleteReviewSuccess() throws Exception {
        Long productId = 1L;
        Long reviewId = 100L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/product/{productId}/review/{reviewId}", productId, reviewId)
                                              .with(SecurityMockMvcRequestPostProcessors.csrf()))
               .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productService).removeReview(
                ArgumentMatchers.eq(productId),
                ArgumentMatchers.eq(reviewId),
                ArgumentMatchers.any()
        );
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testDeleteReviewAccessDenied() throws Exception {
        Long productId = 1L;
        Long reviewId = 100L;

        Mockito.doThrow(new org.springframework.security.access.AccessDeniedException("Not allowed"))
               .when(productService).removeReview(
                       ArgumentMatchers.eq(productId),
                       ArgumentMatchers.eq(reviewId),
                       ArgumentMatchers.any()
               );

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/product/{productId}/review/{reviewId}", productId, reviewId)
                                              .with(SecurityMockMvcRequestPostProcessors.csrf()))
               .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetSubscriptions() throws Exception {
        Userx user2 = new Userx();
        user2.setId(2L);
        user2.setUsername("customer2");
        user2.setPassword("customer2");
        user2.setRole(UserxRole.CUSTOMER);

        Long productId = 1L;
        String name = "Product 1";
        Product product = new Product();
        product.setId(productId);
        product.setName(name);
        product.setPrice(100.0);
        product.setStock(10);

        ProductSubscription subscription = new ProductSubscription();
        subscription.setProduct(product);
        subscription.setUser(user2);
        subscription.addNotifyEvent(ProductEventType.BACK_IN_STOCK);

        Mockito.when(productService.loadProduct(productId)).thenReturn(Optional.of(product));
        Mockito.when(productMapper.mapTo(Mockito.eq(product), Mockito.anyMap())).thenReturn(new ProductDTO(productId, name, 100.0, 10, 0.0, 0.0, null, null, null, null, null, null, null));
        Mockito.when(productSubscriptionRepository.findByProductIdAndUser(productId, user2)).thenReturn(Optional.of(subscription));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/{id}", productId)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(user2)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionRepository).findByProductIdAndUser(ArgumentMatchers.eq(productId), ArgumentMatchers.eq(user2));

    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testSubscribeProductToAll() throws Exception {
        Long productId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product/{id}/subscribe", productId)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionService)
                .addProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.eq(ProductEventType.BACK_IN_STOCK)
                );

        Mockito.verify(productSubscriptionService)
                .addProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.eq(ProductEventType.FOR_SALE)
                );
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testSubscribeProductToInStock() throws Exception {
        Long productId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product/{id}/subscribe", productId)
                        .param("inStock", "true")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionService)
                .addProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.eq(ProductEventType.BACK_IN_STOCK)
                );
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testSubscribeProductToForSale() throws Exception {
        Long productId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product/{id}/subscribe", productId)
                        .param("discount", "true")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionService)
                .addProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.eq(ProductEventType.FOR_SALE)
                );
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testUnsubscribeProduct() throws Exception {
        Long productId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/product/{id}/unsubscribe", productId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionService)
                .deleteProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId)
                );
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testUpdateSubscriptionToInStock() throws Exception {
        Long productId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/product/{id}/subscribe", productId)
                        .param("inStock", "true")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionService)
                .addProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.eq(ProductEventType.BACK_IN_STOCK)
                );

        Mockito.verify(productSubscriptionService)
                .removeProductSubscriptionEvent(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.eq(ProductEventType.FOR_SALE)
                );
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testUpdateSubscriptionToForSale() throws Exception {
        Long productId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/product/{id}/subscribe", productId)
                        .param("discount", "true")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionService)
                .addProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.eq(ProductEventType.FOR_SALE)
                );

        Mockito.verify(productSubscriptionService)
                .removeProductSubscriptionEvent(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.eq(ProductEventType.BACK_IN_STOCK)
                );
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testUpdateSubscriptionToNone() throws Exception {
        Long productId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/product/{id}/subscribe", productId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionService)
                .deleteProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId)
                );
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testSubscriptionAccessDenied() throws Exception {
        Long productId = 1L;

        Mockito.doThrow(new org.springframework.security.access.AccessDeniedException("Not allowed"))
                .when(productSubscriptionService).addProductSubscription(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(productId),
                        ArgumentMatchers.any()
                );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product/{id}/subscribe", productId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
