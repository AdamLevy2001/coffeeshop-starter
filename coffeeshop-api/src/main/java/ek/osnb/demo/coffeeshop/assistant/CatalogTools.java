package ek.osnb.demo.coffeeshop.assistant;

import ek.osnb.demo.coffeeshop.catalog.CatalogApi;
import ek.osnb.demo.coffeeshop.catalog.ProductDetailsInfo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class CatalogTools {

    private final CatalogApi catalogApi;

    CatalogTools(CatalogApi catalogApi) {
        this.catalogApi=catalogApi;
    }

    @Tool(description = "Search for products and return the price, stock and roast level")
    List<ProductDetailsInfo> searchProducts(
            @ToolParam(description = "product name, for example Colombia Huila or a country name Kenya and if empty return all products",
                    required = false) String searchText) {
        return catalogApi.searchProducts(null, searchText);

    }
}
