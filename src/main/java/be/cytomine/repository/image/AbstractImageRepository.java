package be.cytomine.repository.image;

/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.domain.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for the abstract image entity.
 */
@Repository
public interface AbstractImageRepository extends JpaRepository<AbstractImage, Long>, JpaSpecificationExecutor<AbstractImage> {

    @Override
    @EntityGraph(attributePaths = {"uploadedFile"})
    Page<AbstractImage> findAll(@Nullable Specification<AbstractImage> spec, Pageable pageable);


    List<AbstractImage> findAllByUploadedFile(UploadedFile uploadedFile);

    @Query(value = "SELECT DISTINCT ii.baseImage.id FROM ImageInstance ii WHERE ii.project = :project")
    Set<Long> findAllIdsByProject(Project project);


}
