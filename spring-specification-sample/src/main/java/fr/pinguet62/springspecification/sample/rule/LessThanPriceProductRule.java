package fr.pinguet62.springspecification.sample.rule;

import fr.pinguet62.springspecification.core.RuleDescription;
import fr.pinguet62.springspecification.core.RuleName;
import fr.pinguet62.springspecification.core.SpringRule;
import fr.pinguet62.springspecification.core.api.Rule;
import fr.pinguet62.springspecification.core.builder.database.parameter.RuleParameter;
import fr.pinguet62.springspecification.sample.model.Product;
import lombok.Setter;

@SpringRule
@RuleName("Price less than")
@RuleDescription("Test \"$product.amount<$params.amount\"")
public class LessThanPriceProductRule implements Rule<Product> {

    @Setter
    @RuleParameter("amount")
    private Double maximalAmount;

    @Override
    public boolean test(Product product) {
        return product.getPrice() < maximalAmount;
    }

}