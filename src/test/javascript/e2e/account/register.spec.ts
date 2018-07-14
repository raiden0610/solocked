import { browser, element, by } from 'protractor';
import { CommonAction } from './common-action';

describe('account', () => {
    let registerHelper: CommonAction;

    const password = 'Lolmdr06';

    beforeEach(() => {
        browser.get('/');
        registerHelper = new CommonAction();
        browser.waitForAngular();
    });

    it('should register successfuly', () => {
        registerHelper.registerUser('test', password, 'test@test.com');

        element
            .all(by.id('success'))
            .first()
            .isPresent()
            .then(value => {
                expect(value).toEqual(true);
            });

        element
            .all(by.id('success'))
            .first()
            .isDisplayed()
            .then(value => {
                expect(value).toEqual(true);
            });
    });

    it('should not register because email already exist', () => {
        registerHelper.registerUser('test06', password, 'test@test.com');

        browser.driver.sleep(2000);

        element
            .all(by.id('errorEmailExists'))
            .first()
            .isPresent()
            .then(value => {
                expect(value).toEqual(true);
            });

        element
            .all(by.id('errorEmailExists'))
            .first()
            .isDisplayed()
            .then(value => {
                expect(value).toEqual(true);
            });
    });

    it('should not register because username already exist', () => {
        registerHelper.registerUser('test', password, 'test10@test.com');

        browser.driver.sleep(2000);

        element
            .all(by.id('errorUserExists'))
            .first()
            .isPresent()
            .then(value => {
                expect(value).toEqual(true);
            });

        element
            .all(by.id('errorUserExists'))
            .first()
            .isDisplayed()
            .then(value => {
                expect(value).toEqual(true);
            });
    });
});
