/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpModule } from '@angular/http';

import { TreeViewComponent } from './tree-view.component';
import { AlertSeverityHexagonDirective } from '../../../shared/directives/alert-severity-hexagon.directive';
import { CenterEllipsesPipe } from '../../../shared/pipes/center-ellipses.pipe';
import { MetronTableDirective } from '../../../shared/metron-table/metron-table.directive';
import { MetronSorterComponent } from '../../../shared/metron-table/metron-sorter';
import { ColumnNameTranslatePipe } from '../../../shared/pipes/column-name-translate.pipe';
import { AlertSeverityDirective } from '../../../shared/directives/alert-severity.directive';
import { MetronTablePaginationComponent } from '../../../shared/metron-table/metron-table-pagination/metron-table-pagination.component';
import { SearchService } from '../../../service/search.service';
import { MetronDialogBox } from '../../../shared/metron-dialog-box';
import { UpdateService } from '../../../service/update.service';
import { GlobalConfigService } from '../../../service/global-config.service';
import { MetaAlertService } from '../../../service/meta-alert.service';

describe('TreeViewComponent', () => {
  let component: TreeViewComponent;
  let fixture: ComponentFixture<TreeViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ HttpModule ],
      providers: [
        SearchService,
        UpdateService,
        GlobalConfigService,
        MetaAlertService,
        MetronDialogBox,
      ],
      declarations: [ 
        MetronTableDirective,
        MetronSorterComponent,
        MetronTablePaginationComponent,
        AlertSeverityHexagonDirective,
        AlertSeverityDirective,
        CenterEllipsesPipe,
        ColumnNameTranslatePipe,
        TreeViewComponent,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TreeViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
