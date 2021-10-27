package be.cytomine.service.ontology;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.command.*;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.Relation;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.SecUser;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.ontology.RelationRepository;
import be.cytomine.repository.ontology.RelationTermRepository;
import be.cytomine.repository.ontology.TermRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

import static org.springframework.security.acls.domain.BasePermission.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RelationTermService extends ModelService {

    private final RelationTermRepository relationTermRepository;

    private final SecurityACLService securityACLService;

    private final CurrentUserService currentUserService;

    @Override
    public Class currentDomain() {
        return RelationTerm.class;
    }

    /**
     * Get a relation term
     */
    public Optional<RelationTerm> find(Relation relation, Term term1, Term term2) {
        securityACLService.check(term1.container(),READ);
        securityACLService.check(term2.container(),READ);
        return relationTermRepository.findByRelationAndTerm1AndTerm2(relation, term1, term2);
    }

    /**
     * List all relation term for a specific term (position 1 or 2)
     * @param term Term filter
     * @param position Term position in relation (term x PARENT term y => term x position 1, term y position 2)
     * @return Relation term list
     */
    public List<RelationTerm> list(Term term, String position) {
        securityACLService.check(term.container(),READ);
        return position.equals("1") ? relationTermRepository.findAllByTerm1(term) : relationTermRepository.findAllByTerm2(term);
    }

    /**
     * List all relation term for a specific term (position 1 or 2)
     * @param term Term filter
     * @return Relation term list
     */
    public List<RelationTerm> list(Term term) {
        securityACLService.check(term.container(),READ);
        return relationTermRepository.findAllByTerm(term);
    }

    /**
     * Add the new domain with JSON data
     * @param jsonObject New domain data
     * @return Response structure (created domain data,..)
     */
    @Override
    public CommandResponse add(JsonObject jsonObject) {
        securityACLService.check(jsonObject.getJSONAttrLong("term1"),Term.class,WRITE);
        securityACLService.check(jsonObject.getJSONAttrLong("term2"),Term.class,WRITE);
        SecUser currentUser = currentUserService.getCurrentUser();
        return executeCommand(new AddCommand(currentUser),null,jsonObject);
    }
    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        SecUser currentUser = currentUserService.getCurrentUser();
        securityACLService.check(domain.container(),DELETE);
        Command c = new DeleteCommand(currentUser, transaction);
        return executeCommand(c,domain, null);
    }


    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new RelationTerm().buildDomainFromJson(json, getEntityManager());
    }


    @Override
    public List<String> getStringParamsI18n(CytomineDomain domain) {
        RelationTerm rt = (RelationTerm)domain;
        return Arrays.asList(String.valueOf(rt.getId()), rt.getRelation().getName(), rt.getTerm1().getName(), rt.getTerm2().getName());
    }

    @Override
    public CommandResponse update(CytomineDomain domain, JsonObject jsonNewData, Transaction transaction) {
        throw new RuntimeException("Update is not implemented for Relation Term");
    }


    public void checkDoNotAlreadyExist(CytomineDomain domain){
        RelationTerm relationTerm = (RelationTerm)domain;
        if(relationTerm!=null && relationTerm.getRelation()!=null&& relationTerm.getTerm2()!=null&& relationTerm.getTerm2()!=null) {
            if(relationTermRepository.findByRelationAndTerm1AndTerm2(relationTerm.getRelation(), relationTerm.getTerm1(), relationTerm.getTerm2()).stream().anyMatch(x -> !Objects.equals(x.getId(), relationTerm.getId())))  {
                throw new AlreadyExistException("RelationTerm with relation=" + relationTerm.getRelation().getId() + " and term1 " + relationTerm.getTerm1().getId() + " and term2 " + relationTerm.getTerm2().getId() + " already exist!");
            }
        }
    }


    public void deleteDependencies(CytomineDomain domain, Transaction transaction, Task task) {
        return;
    }


    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    @Override
    public CytomineDomain retrieve(JsonObject json) {
        return relationTermRepository.findByRelationAndTerm1AndTerm2(json.getJSONAttrLong("relation"),json.getJSONAttrLong("term1"),json.getJSONAttrLong("term2"))
                .orElseThrow(() -> new ObjectNotFoundException("Relation-term not found " + json));
    }
}
