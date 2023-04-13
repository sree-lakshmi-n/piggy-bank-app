import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class CustomerPortalDepositRoute extends Route {
  @service('auth') auth;
}
