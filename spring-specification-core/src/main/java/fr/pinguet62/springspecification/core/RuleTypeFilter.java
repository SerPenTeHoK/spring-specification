package fr.pinguet62.springspecification.core;

import fr.pinguet62.springspecification.core.api.Rule;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * {@link Predicate} to check if a {@link Rule} can be applied on {@link Class}.
 */
@RequiredArgsConstructor
public class RuleTypeFilter implements Predicate<Class<Rule<?>>> {

    @NonNull
    private final Class<?> modelType;

    @Override
    public boolean test(Class<Rule<?>> ruleType) {
        requireNonNull(ruleType);
        ResolvableType resolvableType = ResolvableType.forClass(ruleType).as(Rule.class);
        if (resolvableType.resolve() == null)
            return false; // not a Rule<?>
        resolvableType = resolvableType.getGeneric(0);
        Class<?> resolvedType = resolvableType.resolve();
        if (resolvedType == null)
            return true; // generic without type restriction
        return resolvedType.isAssignableFrom(modelType);
    }

}
