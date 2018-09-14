import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { JhiEventManager } from 'ng-jhipster';
import { LoginService } from 'app/core/login/login.service';
import { Principal } from 'app/core';
import { AccountsDBService } from 'app/entities/accounts-db';
import { CryptoService } from 'app/shared/crypto/crypto.service';
import { Observable } from 'rxjs';

@Component({
    selector: 'jhi-login-modal',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class JhiLoginModalComponent {
    authenticationError: boolean;
    password: string;
    authenticationKey: string;
    rememberMe: boolean;
    username: string;
    credentials: any;
    loading: boolean;

    constructor(
        private eventManager: JhiEventManager,
        private loginService: LoginService,
        private router: Router,
        private accountService: AccountsDBService,
        private cryptoService: CryptoService,
        private principal: Principal
    ) {
        this.credentials = {};
    }

    cancel() {
        this.credentials = {
            username: null,
            password: null,
            rememberMe: true
        };
        this.authenticationError = false;
        // this.activeModal.dismiss('cancel');
    }

    login() {
        this.loading = true;
        this.loginService.prelogin(this.username, this.password).subscribe(
            (step2Result: any) => {
                if (step2Result === null) {
                    this.loading = false;
                    this.authenticationError = true;
                } else {
                    this.loginJHI(step2Result.a, step2Result.M1);
                    // this.authenticationKey = accounts.authenticationKey;
                    // this.authenticationError = false;
                    // accounts.authenticationKey = '';
                    // this.accountService.saveOnBrowser(accounts);
                    // this.loginJHI();
                }
            },
            error => {
                this.authenticationError = true;
                this.loading = false;
            }
        );
    }

    loginJHI(a: string, m1: string) {
        this.loginService
            .login({
                username: this.username,
                password: a + ':' + m1,
                rememberMe: this.rememberMe
            })
            .then(() => {
                this.loading = false;
                this.authenticationError = false;

                this.eventManager.broadcast({
                    name: 'authenticationSuccess',
                    content: 'Sending Authentication Success'
                });

                if (this.principal.hasAnyAuthorityDirect(['ROLE_ADMIN'])) {
                    this.router.navigate(['/user-management']);
                } else {
                    // Loading of the encrypted DB
                    Observable.fromPromise(this.cryptoService.creatingKey(this.password))
                        .flatMap(cryptoKey => this.cryptoService.putCryptoKeyInStorage(cryptoKey))
                        .flatMap(result => this.accountService.synchroDB())
                        .subscribe(accounts => {
                            this.accountService.saveOnBrowser(accounts);
                        });

                    this.router.navigate(['/accounts']);
                }
                // previousState was set in the authExpiredInterceptor before being redirected to login modal.
                // since login is succesful, go to stored previousState and clear previousState
                /*const redirect = this.stateStorageService.getUrl();
        if (redirect) {
            this.router.navigate([redirect]);
        }*/
            })
            .catch(() => {
                this.authenticationError = true;
                this.loading = false;
            });
    }

    requestResetPassword() {
        // this.activeModal.dismiss('to state requestReset');
        this.router.navigate(['/reset', 'request']);
    }

    register() {
        this.router.navigate(['/register']);
    }
}
