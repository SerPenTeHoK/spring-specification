package fr.pinguet62.springspecification.admin.server;

import fr.pinguet62.springspecification.admin.server.dto.BusinessRuleDto;
import fr.pinguet62.springspecification.admin.server.dto.BusinessRuleInputDto;
import fr.pinguet62.springspecification.core.api.FailingRule;
import fr.pinguet62.springspecification.core.api.Rule;
import fr.pinguet62.springspecification.core.builder.database.model.BusinessRuleEntity;
import fr.pinguet62.springspecification.core.builder.database.model.RuleComponentEntity;
import fr.pinguet62.springspecification.core.builder.database.repository.BusinessRuleRepository;
import fr.pinguet62.springspecification.core.builder.database.repository.RuleComponentRepository;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static fr.pinguet62.springspecification.admin.server.BusinessRuleController.PATH;
import static fr.pinguet62.springspecification.core.builder.database.autoconfigure.SpringSpecificationBeans.TRANSACTION_MANAGER_NAME;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.util.UriUtils.encode;

@Transactional(TRANSACTION_MANAGER_NAME)
@RestController
@RequestMapping(PATH)
public class BusinessRuleController {

    public static final String PATH = "/businessRule";

    @Autowired
    private BusinessRuleRepository businessRuleRepository;

    @Autowired
    private RuleComponentRepository ruleComponentRepository;

    @Autowired
    private RuleService ruleService;

    @GetMapping
    public @NotNull @Valid List<BusinessRuleDto> getAll() {
        return businessRuleRepository.findAll().stream().map(this::convert).collect(toList());
    }

    @GetMapping("/{id:.+}")
    public @Valid ResponseEntity<BusinessRuleDto> getById(@NotBlank @PathVariable String id) {
        Optional<BusinessRuleEntity> entity = businessRuleRepository.findById(id);
        if (!entity.isPresent())
            return ResponseEntity.status(NOT_FOUND).build();

        return ResponseEntity.ok(convert(entity.get()));
    }

    @PutMapping
    public @Valid ResponseEntity<BusinessRuleDto> create(@Valid @RequestBody BusinessRuleInputDto dto) throws UnsupportedEncodingException {
        RuleComponentEntity rootRuleComponent = new RuleComponentEntity();
        rootRuleComponent.setParent(null /*root*/);
        rootRuleComponent.setIndex(0);
        rootRuleComponent.setKey(ruleService.getKey((Class<Rule<?>>) (Class<?>) FailingRule.class)); // TODO fix generic
        rootRuleComponent.setDescription(null);
        rootRuleComponent = ruleComponentRepository.save(rootRuleComponent);

        BusinessRuleEntity entity = new BusinessRuleEntity();
        entity.setId(dto.getId());
        entity.setArgumentType(dto.getArgumentType());
        entity.setRootRuleComponent(rootRuleComponent);
        entity.setTitle(dto.getTitle());
        entity = businessRuleRepository.save(entity);

        return ResponseEntity
                .created(URI.create(encode(PATH + "/" + entity.getId(), defaultCharset().name())))
                .body(convert(entity));
    }

    @DeleteMapping("/{id:.+}")
    public @Valid ResponseEntity<BusinessRuleDto> delete(@NotBlank @PathVariable String id) {
        Optional<BusinessRuleEntity> entity = businessRuleRepository.findById(id);
        if (!entity.isPresent())
            return ResponseEntity.notFound().build();

        BusinessRuleDto dto = convert(entity.get());

        businessRuleRepository.deleteById(id);

        return ResponseEntity.ok(dto);
    }

    private BusinessRuleDto convert(BusinessRuleEntity entity) {
        // @formatter:off
        return BusinessRuleDto
                .builder()
                .id(entity.getId())
                .argumentType(entity.getArgumentType())
                .rootRuleComponent(RuleComponentController.convert(entity.getRootRuleComponent()))
                .title(entity.getTitle())
                .build();
        // @formatter:on
    }

}