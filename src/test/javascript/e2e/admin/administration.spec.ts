import { browser, element, by, ExpectedConditions as ec } from 'protractor';

import { NavBarPage, SignInPage } from '../page-objects/jhi-page-objects';
import { CommonAction } from '../account/common-action';

const expect = chai.expect;

describe('administration', () => {
    let navBarPage: NavBarPage;
    let registerHelper: CommonAction;

    before(async () => {
        await browser.get('/');
        navBarPage = new NavBarPage(true);
        registerHelper = new CommonAction();
        await registerHelper.login('admin', 'admin', false);
        await browser.sleep(500);
    });

    beforeEach(async () => {
        await browser.wait(ec.visibilityOf(navBarPage.adminMenu), 10000);
        await navBarPage.clickOnAdminMenu();
    });

    it('should load user management', async () => {
        await navBarPage.clickOnAdmin('user-management');
        const expect1 = 'userManagement.home.title';
        const value1 = await element(by.id('user-management-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load metrics', async () => {
        await navBarPage.clickOnAdmin('jhi-metrics');
        const expect1 = 'metrics.title';
        const value1 = await element(by.id('metrics-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load health', async () => {
        await navBarPage.clickOnAdmin('jhi-health');
        const expect1 = 'health.title';
        const value1 = await element(by.id('health-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load configuration', async () => {
        await navBarPage.clickOnAdmin('jhi-configuration');
        await browser.sleep(500);
        const expect1 = 'configuration.title';
        const value1 = await element(by.id('configuration-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load audits', async () => {
        await navBarPage.clickOnAdmin('audits');
        await browser.sleep(500);
        const expect1 = 'audits.title';
        const value1 = await element(by.id('audits-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load logs', async () => {
        await navBarPage.clickOnAdmin('logs');
        await browser.sleep(500);
        const expect1 = 'logs.title';
        const value1 = await element(by.id('logs-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    after(async () => {
        await registerHelper.logout();
    });
});
