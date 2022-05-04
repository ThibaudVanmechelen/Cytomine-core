package be.cytomine.service.dto;

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

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WindowParameter {

    private String format;

    private int x;

    private int y;

    private int w;

    private int h;

    private boolean withExterior;

    private BoundariesCropParameter boundaries;
}
