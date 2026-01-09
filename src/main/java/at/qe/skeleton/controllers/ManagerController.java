package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.OrderDTO;
import at.qe.skeleton.dtos.PageableListDTO;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.mappers.OrderMapper;
import at.qe.skeleton.mappers.ProductMapper;
import at.qe.skeleton.model.Order;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.services.OrderService;
import at.qe.skeleton.services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/manager")
public class ManagerController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Autowired
    public ManagerController(ProductService productService, ProductMapper productMapper, OrderService orderService, OrderMapper orderMapper) {
        this.productService = productService;
        this.productMapper = productMapper;

        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    /**
     * Creates a product
     *
     * @param productDto the user tb created
     * @return {@link ResponseEntity} with status {@code 201 (Created)} with the newly created product in the body
     */
    @PostMapping("/createProduct")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDto) {
        Product product = productService.saveProduct(productMapper.mapFrom(productDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.mapTo(product));
    }

    /**
     * Updates a product
     *
     * @param id the id of the product tb updated
     * @param productUpdateDTO updated version of that product
     * @return {@link ResponseEntity} with status {@code 201 (Created)} with the updated product in the body, or with status {@code 404 (Not Found)} if no product with this id exists
     */
    @PatchMapping("/product/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productUpdateDTO) {
        Product existingProduct = productService.loadProduct(id).orElseThrow(EntityNotFoundException::new);

        productMapper.updateProductFromDto(productUpdateDTO, existingProduct);

        Product savedProduct = productService.saveProduct(existingProduct);

        return ResponseEntity.ok(productMapper.mapTo(savedProduct));
    }

    /**
     * Deletes product of given id.
     *
     * @param id the id of the product tb deleted
     * @return {@link ResponseEntity} with status {@code 204 (No Content)} on successful delete, or with status {@code 404 (Not Found)} if no product with this id exists
     */

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Product existingProduct = productService.loadProduct(id).orElseThrow(EntityNotFoundException::new);

        productService.deleteProduct(existingProduct);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET all orders
     *
     * @param pageId id of page (0 indexed) or null
     * @param pageSize size of page or null
     * @param sort how the output should be sorted
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with a collection of products on
     *         the specified page with the specified filters and sorting
     */
    @GetMapping("/orders")
    public ResponseEntity<PageableListDTO<OrderDTO>> getAllOrders(
            @RequestParam(required = false) Integer pageId,
            @RequestParam(required = false) Integer pageSize,
            @SortDefault(sort = "createdDate", direction = Sort.Direction.ASC) Sort sort
            ) {

        Sort finalSort = (sort != null) ? sort : Sort.unsorted();

        Pageable pageable = (pageId != null && pageSize != null && pageSize > 0)
                ? PageRequest.of(pageId, pageSize, finalSort)
                : Pageable.unpaged();

        Page<Order> orderPage = orderService.getAllOrders(pageable);

        PageableListDTO<OrderDTO> pageableListDTO = new PageableListDTO<>(
                pageSize,
                (pageId != null) ? pageId + 1 : null,
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                orderPage.getContent().stream().map(orderMapper::mapTo).toList()
        );

        return ResponseEntity.ok(pageableListDTO);
    }
}
