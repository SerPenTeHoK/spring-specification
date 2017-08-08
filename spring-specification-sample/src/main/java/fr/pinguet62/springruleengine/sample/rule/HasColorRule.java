package fr.pinguet62.springruleengine.sample.rule;

import fr.pinguet62.springruleengine.core.Context;
import fr.pinguet62.springruleengine.core.RuleDescription;
import fr.pinguet62.springruleengine.core.RuleName;
import fr.pinguet62.springruleengine.core.api.Rule;
import fr.pinguet62.springruleengine.core.builder.database.parameter.RuleParameter;
import fr.pinguet62.springruleengine.sample.model.Product;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RuleName("Has color")
@RuleDescription("Test \"$context.product.color=$params.color\"")
public class HasColorRule implements Rule {

    @Setter
    @RuleParameter("color")
    private String expectedColor;

    @Override
    public boolean test(Context context) {
        Product product = context.get("product", Product.class);
        String actualColor = product.getColor();

        return actualColor.equals(expectedColor);
    }

}