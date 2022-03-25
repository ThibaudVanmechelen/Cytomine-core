package be.cytomine.service.meta;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.command.*;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.meta.AttachedFile;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.SecUser;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.CytomineMethodNotYetImplementedException;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.repository.meta.AttachedFileRepository;
import be.cytomine.repository.ontology.AnnotationDomainRepository;
import be.cytomine.repository.ontology.AnnotationTermRepository;
import be.cytomine.repository.ontology.RelationRepository;
import be.cytomine.repository.ontology.TermRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.ontology.RelationTermService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.security.acls.domain.BasePermission.*;

@Slf4j
@Service
@Transactional
public class AttachedFileService extends ModelService {

    @Autowired
    private AttachedFileRepository attachedFileRepository;

    @Autowired
    private SecurityACLService securityACLService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private AnnotationDomainRepository annotationDomainRepository;

    @Override
    public Class currentDomain() {
        return AttachedFile.class;
    }

    public List<AttachedFile> list() {
        securityACLService.checkAdmin(currentUserService.getCurrentUser());
        return attachedFileRepository.findAll();
    }

    public List<AttachedFile> findAllByDomain(CytomineDomain domain) {
        return findAllByDomain(domain.getClass().getName(), domain.getId());
    }

    public List<AttachedFile> findAllByDomain(String domainClassName, Long domainIdent) {
        if(domainClassName.contains("AnnotationDomain")) {
            AnnotationDomain annotation = annotationDomainRepository.findById(domainIdent)
                    .orElseThrow(() -> new ObjectNotFoundException(domainClassName, domainIdent));
            securityACLService.check(annotation, READ);
        } else {
            securityACLService.check(domainIdent,domainClassName, READ);
        }
        return attachedFileRepository.findAllByDomainClassNameAndDomainIdent(domainClassName, domainIdent);
    }

    public Optional<AttachedFile> findById(Long id) {
        Optional<AttachedFile> attachedFile = attachedFileRepository.findById(id);
        attachedFile.ifPresent(file -> securityACLService.check(file.getDomainIdent(),file.getDomainClassName(),READ));
        return attachedFile;
    }

    public AttachedFile create(String filename,byte[] data, String key, Long domainIdent,String domainClassName) throws ClassNotFoundException {
        securityACLService.checkUser(currentUserService.getCurrentUser());
        CytomineDomain recipientDomain = (CytomineDomain)getEntityManager().find(Class.forName(domainClassName), domainIdent);

        if (recipientDomain instanceof AbstractImage) {
            securityACLService.check(domainIdent,domainClassName,READ);
        } else if(recipientDomain instanceof Project || !(recipientDomain.container() instanceof Project)) {
            securityACLService.check(domainIdent,domainClassName,WRITE);
        } else {
            securityACLService.checkFullOrRestrictedForOwner(domainIdent,domainClassName, "user");
        }

        AttachedFile file = new AttachedFile();
        file.setDomainIdent(domainIdent);
        file.setDomainClassName(domainClassName);
        file.setFilename(filename);
        file.setData(data);
        file.setKey(key);
        saveDomain(file);
        return file;
    }

    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        SecUser currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        AttachedFile attachedFile = (AttachedFile)domain;

        CytomineDomain recipientDomain = getCytomineDomain(attachedFile.getDomainClassName(), attachedFile.getDomainIdent());
        if (recipientDomain == null) {
            throw new ObjectNotFoundException(attachedFile.getDomainClassName(), attachedFile.getDomainIdent());
        }

        securityACLService.check(attachedFile.getDomainIdent(),attachedFile.getDomainClassName(),READ);
        if (recipientDomain instanceof AbstractImage) {
            securityACLService.check(attachedFile.getDomainIdent(),attachedFile.getDomainClassName(),READ);
        } else if(recipientDomain instanceof Project || !(recipientDomain.container() instanceof Project)) {
            securityACLService.check(attachedFile.getDomainIdent(),attachedFile.getDomainClassName(),DELETE);
        } else {
            securityACLService.checkFullOrRestrictedForOwner(attachedFile.getDomainIdent(),attachedFile.getDomainClassName(), "user");
        }
        Command c = new DeleteCommand(currentUser, transaction);
        return executeCommand(c,domain, null);
    }


    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return null;
    }


    @Override
    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        return List.of(domain.getId(), ((AttachedFile)domain).getDomainClassName());
    }
}
