/* tslint:disable max-line-length */
import { ComponentFixture, fakeAsync, inject, TestBed, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { NinjaccountTestModule } from '../../../test.module';
import { SrpDeleteDialogComponent } from 'app/entities/srp/srp-delete-dialog.component';
import { SrpService } from 'app/entities/srp/srp.service';

describe('Component Tests', () => {
    describe('Srp Management Delete Component', () => {
        let comp: SrpDeleteDialogComponent;
        let fixture: ComponentFixture<SrpDeleteDialogComponent>;
        let service: SrpService;
        let mockEventManager: any;
        let mockActiveModal: any;

        beforeEach(() => {
            TestBed.configureTestingModule({
                imports: [NinjaccountTestModule],
                declarations: [SrpDeleteDialogComponent]
            })
                .overrideTemplate(SrpDeleteDialogComponent, '')
                .compileComponents();
            fixture = TestBed.createComponent(SrpDeleteDialogComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(SrpService);
            mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
            mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
        });

        describe('confirmDelete', () => {
            it(
                'Should call delete service on confirmDelete',
                inject(
                    [],
                    fakeAsync(() => {
                        // GIVEN
                        spyOn(service, 'delete').and.returnValue(of({}));

                        // WHEN
                        comp.confirmDelete(123);
                        tick();

                        // THEN
                        expect(service.delete).toHaveBeenCalledWith(123);
                        expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
                        expect(mockEventManager.broadcastSpy).toHaveBeenCalled();
                    })
                )
            );
        });
    });
});
