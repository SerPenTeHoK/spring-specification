package fr.pinguet62.springruleengine.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import org.hibernate.validator.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessRuleDto {

    @NotBlank
    private String id;

    @NotBlank
    private String argumentType;

    @NotNull
    @Valid
    private RuleComponentDto rootRuleComponent;

    private String title;

}