import { Component, OnInit } from '@angular/core';
import { ActivatedRouteSnapshot, NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { Breadcrumb } from 'app/layouts/breadcrumb/breadcrumb.model';
import { AccountService } from 'app/core';
import { AccountsHomeRouteName } from 'app/connected';
import { Observable } from 'rxjs';
import { distinctUntilChanged, filter, map } from 'rxjs/operators';

@Component({
    selector: 'jhi-breadcrumb',
    templateUrl: './breadcrumb.component.html',
    styleUrls: ['./breadcrumb.component.scss'],
    animations: [
        trigger('appear', [
            state('void', style({ opacity: 0.0 })),
            state('*', style({ opacity: 1 })),
            transition('void => *, * => void', animate('500ms  ease-in-out'))
        ])
    ]
})
export class BreadcrumbComponent implements OnInit {
    breadcrumbSteps: Array<Breadcrumb>;

    constructor(private router: Router, private translateService: TranslateService, private accountService: AccountService) {
        this.breadcrumbSteps = [];
    }

    ngOnInit() {
        this.router.events
            .pipe(
                filter(event => event instanceof NavigationEnd),
                distinctUntilChanged()
            )
            .subscribe((event: NavigationEnd) => {
                if (this.breadcrumbSteps.length === 0 && event.url !== AccountsHomeRouteName) {
                    // You come from a page reload, or directly accessing a deep page
                    for (const config of this.router.config) {
                        if (config.path === AccountsHomeRouteName) {
                            this.createNewStep('/' + AccountsHomeRouteName, <ActivatedRouteSnapshot>config).subscribe(newStep =>
                                this.breadcrumbSteps.push(newStep)
                            );
                        }
                    }
                }

                if (event.url === '/') {
                    // Clear the breadcrumb, we are on the homepage
                    this.breadcrumbSteps.splice(0, this.breadcrumbSteps.length);
                } else {
                    // We are on an authenticated page (accounts, add account, etc)
                    this.createNewStep(event.url, this.router.routerState.snapshot.root).subscribe(newStep => {
                        let present = false;
                        let idPresent = -1;

                        // Analyzing if the steps is present
                        for (let i = 0; i < this.breadcrumbSteps.length; i++) {
                            const step = this.breadcrumbSteps[i];
                            if (step.url === newStep.url) {
                                present = true;
                                idPresent = i;
                            }
                        }

                        // If there is more than 2 steps, it's not correct because for now we just manage one level of route
                        if (this.breadcrumbSteps.length >= 2) {
                            // We clear the second elements
                            this.breadcrumbSteps.splice(1, this.breadcrumbSteps.length);
                        }

                        if (!present) {
                            this.breadcrumbSteps.push(newStep);
                        }
                    });
                }
            });
    }

    private getPageTitle(routeSnapshot: ActivatedRouteSnapshot): string {
        let title: string = routeSnapshot.data && routeSnapshot.data['pageTitle'] ? routeSnapshot.data['pageTitle'] : 'ninjaccountApp';
        if (routeSnapshot.firstChild) {
            title = this.getPageTitle(routeSnapshot.firstChild) || title;
        }
        return title;
    }

    isAuthenticated() {
        return this.accountService.isAuthenticated();
    }

    hasAnyAuthorityDirect(authorities: string[]): boolean {
        return this.accountService.hasAnyAuthority(authorities);
    }

    createNewStep(url: string, routeSnapshot: ActivatedRouteSnapshot): Observable<Breadcrumb> {
        const labelKey = this.getPageTitle(routeSnapshot);
        return this.translateService.get(labelKey).pipe(map(title => new Breadcrumb(title, url)));
    }
}
