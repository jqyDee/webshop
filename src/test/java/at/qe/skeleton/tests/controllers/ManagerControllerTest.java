package at.qe.skeleton.tests.controllers;

import at.qe.skeleton.configs.JwtConfig;
import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.configs.TokenAuthenticationFilter;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.mappers.OrderMapper;
import at.qe.skeleton.mappers.ProductMapper;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.services.OrderService;
import at.qe.skeleton.services.ProductService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoSpyBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @MockitoBean
    private JwtConfig jwtConfig;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

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
        Mockito.when(mockClaims.getSubject()).thenReturn("admin");
        Mockito.when(mockJws.getPayload()).thenReturn(mockClaims);
        Mockito.when(jwtTokenProvider.validateTokenAndGetJws(Mockito.anyString()))
                .thenReturn(Optional.of(mockJws));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testCreateProduct() throws Exception {
        ProductDTO mockProductDTO = new ProductDTO(
                1L, "mock", 100.5, 2, 0, 0.0, null, null, null, null, null, null, null
        );

        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("mock");
        mockProduct.setPrice(100.5);
        mockProduct.setStock(2);
        mockProduct.setDiscount(0);

        Mockito.when(productMapper.mapFrom(Mockito.any(ProductDTO.class))).thenReturn(mockProduct);
        Mockito.when(productService.saveProduct(Mockito.any(Product.class))).thenReturn(mockProduct);
        Mockito.when(productMapper.mapTo(Mockito.any(Product.class))).thenReturn(mockProductDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/manager/createProduct")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockProductDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("mock"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(100.5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stock").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.discount").value(0));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testDeleteProductExists() throws Exception {
        Long produtctId = 1L;

        Product p1 = new Product();
        p1.setId(produtctId);

        Mockito.when(productService.loadProduct(ArgumentMatchers.eq(produtctId)))
               .thenReturn(Optional.of(p1));

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/manager/product/{productId}", produtctId)
                                      .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productService).deleteProduct(ArgumentMatchers.eq(p1));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testDeleteProductDoesNotExist() throws Exception {
        Long productId = 1L;

        Mockito.when(productService.loadProduct(ArgumentMatchers.eq(productId))).thenReturn(Optional.empty());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/manager/product/{productId}", productId).
                        with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    public void testUpdateProduct() throws Exception {
        Long productId = 1L;

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("mock");

        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("updated product");

        ProductDTO productUpdateDto = new ProductDTO(1L, "updated product", 10.0, 1, 0.0, 0.0, null, null, null, null, null, null, null);

        Mockito.when(productService.loadProduct(productId)).thenReturn(Optional.of(existingProduct));
        Mockito.when(productService.saveProduct(existingProduct)).thenReturn(updatedProduct);

        Mockito.when(productMapper.mapTo(updatedProduct)).thenReturn(productUpdateDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/manager/product/{productId}", productId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productUpdateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("updated product"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(10.0));
    }
}
