package fr.pinguet62.springruleengine.core.config;

import fr.pinguet62.springruleengine.core.builder.database.factory.RuleAutowireCandidateResolver;
import fr.pinguet62.springruleengine.core.builder.database.parameter.RuleParameterAutowireCandidateResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Register all custom {@link AutowireCandidateResolver}s.
 */
@Component
public class RuleParameterBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    /**
     * Append custom {@link AutowireCandidateResolver}s to beginning of default.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        AutowireCandidateResolver autowireCandidateResolver = new CompositeAutowireCandidateResolver(
                new RuleParameterAutowireCandidateResolver(),
                new RuleAutowireCandidateResolver(),
                defaultListableBeanFactory.getAutowireCandidateResolver()
        );
        defaultListableBeanFactory.setAutowireCandidateResolver(autowireCandidateResolver);
    }

}