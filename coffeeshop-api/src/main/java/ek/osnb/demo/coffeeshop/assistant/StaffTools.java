package ek.osnb.demo.coffeeshop.assistant;

import ek.osnb.demo.coffeeshop.catalog.CatalogApi;
import ek.osnb.demo.coffeeshop.catalog.ProductDetailsInfo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class StaffTools {

    private final CatalogApi catalogApi;

    StaffTools(CatalogApi catalogApi) {
        this.catalogApi=catalogApi;
    }

    @Tool(description = "get the products with low stocks, so the Staff can order more")
    List<ProductDetailsInfo> getAllLowStocks() {
        List<ProductDetailsInfo> lowStocks = new ArrayList<>();

        for (ProductDetailsInfo product : catalogApi.searchProducts(null, null)) {
            if (product.stock() < 10) {
                lowStocks.add(product);
            }
        }
        return lowStocks;
    }
}
