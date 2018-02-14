/* tslint:disable max-line-length */
import {async, ComponentFixture, fakeAsync, inject, TestBed, tick} from '@angular/core/testing';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Observable} from 'rxjs/Observable';
import {JhiEventManager} from 'ng-jhipster';

import {NinjaccountTestModule} from '../../../test.module';
import {AccountsDBDeleteDialogComponent} from '../../../../../../main/webapp/app/entities/accounts-db/accounts-db-delete-dialog.component';
import {AccountsDBService} from '../../../../../../main/webapp/app/entities/accounts-db/accounts-db.service';

describe('Component Tests', () => {

    describe('AccountsDB Management Delete Component', () => {
        let comp: AccountsDBDeleteDialogComponent;
        let fixture: ComponentFixture<AccountsDBDeleteDialogComponent>;
        let service: AccountsDBService;
        let mockEventManager: any;
        let mockActiveModal: any;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [NinjaccountTestModule],
                declarations: [AccountsDBDeleteDialogComponent],
                providers: [
                    AccountsDBService
                ]
            })
            .overrideTemplate(AccountsDBDeleteDialogComponent, '')
            .compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(AccountsDBDeleteDialogComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(AccountsDBService);
            mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
            mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
        });

        describe('confirmDelete', () => {
            it('Should call delete service on confirmDelete',
                inject([],
                    fakeAsync(() => {
                        // GIVEN
                        spyOn(service, 'delete').and.returnValue(Observable.of({}));

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
