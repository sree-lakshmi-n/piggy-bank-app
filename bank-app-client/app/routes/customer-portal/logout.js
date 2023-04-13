import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';

export default class CustomerPortalLogoutRoute extends Route {
  @service router;
  @service('auth') auth;
}
