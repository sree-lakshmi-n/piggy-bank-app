import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class CustomerPortalRoute extends Route {
  @service('auth') auth;
  @service router;

  async model() {
    return [
      'profile',
      'edit-contact',
      'deposit',
      'withdraw',
      'transfer',
      'logout',
    ];
  }
  afterModel() {
    if (this.auth.get('currentCustId') === null) {
      this.router.transitionTo('index');
    }
  }
}
