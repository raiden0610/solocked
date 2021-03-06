import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ISrp } from 'app/shared/model/srp.model';
import { SrpService } from './srp.service';

@Component({
    selector: 'jhi-srp-delete-dialog',
    templateUrl: './srp-delete-dialog.component.html'
})
export class SrpDeleteDialogComponent {
    srp: ISrp;

    constructor(protected srpService: SrpService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id: number) {
        this.srpService.delete(id).subscribe(response => {
            this.eventManager.broadcast({
                name: 'srpListModification',
                content: 'Deleted an srp'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-srp-delete-popup',
    template: ''
})
export class SrpDeletePopupComponent implements OnInit, OnDestroy {
    protected ngbModalRef: NgbModalRef;

    constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

    ngOnInit() {
        this.activatedRoute.data.subscribe(({ srp }) => {
            setTimeout(() => {
                this.ngbModalRef = this.modalService.open(SrpDeleteDialogComponent as Component, {
                    size: 'lg',
                    backdrop: 'static'
                });
                this.ngbModalRef.componentInstance.srp = srp;
                this.ngbModalRef.result.then(
                    result => {
                        this.router.navigate(['/srp', { outlets: { popup: null } }]);
                        this.ngbModalRef = null;
                    },
                    reason => {
                        this.router.navigate(['/srp', { outlets: { popup: null } }]);
                        this.ngbModalRef = null;
                    }
                );
            }, 0);
        });
    }

    ngOnDestroy() {
        this.ngbModalRef = null;
    }
}
