package be.cytomine.domain.processing;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterConstraint extends CytomineDomain {

    @NotNull
    @NotBlank
    @Column(nullable = false, unique = false)
    private String name;

    @NotNull
    @NotBlank
    private String expression;

    @NotNull
    @NotBlank
    private String dataType;



    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        ParameterConstraint parameterConstraint = (ParameterConstraint)this;
        parameterConstraint.id = json.getJSONAttrLong("id",null);
        parameterConstraint.name = json.getJSONAttrStr("name");
        parameterConstraint.expression = json.getJSONAttrStr("expression");
        parameterConstraint.dataType = json.getJSONAttrStr("dataType");
        parameterConstraint.created = json.getJSONAttrDate("created");
        parameterConstraint.updated = json.getJSONAttrDate("updated");
        return parameterConstraint;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        ParameterConstraint parameterConstraint = (ParameterConstraint)domain;
        returnArray.put("name", parameterConstraint.getName());
        returnArray.put("expression", parameterConstraint.getExpression());
        returnArray.put("dataType", parameterConstraint.getDataType());
        return returnArray;
    }


    @Override
    public String toJSON() {
        return toJsonObject().toJsonString();
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

}
