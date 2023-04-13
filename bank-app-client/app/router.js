import EmberRouter from '@ember/routing/router';
import config from 'bank-app-client/config/environment';

export default class Router extends EmberRouter {
  location = config.locationType;
  rootURL = config.rootURL;
}

Router.map(function () {
  this.route('register');
  this.route('customer-portal', function () {
    this.route('profile');
    this.route('deposit');
    this.route('withdraw');
    this.route('transfer');
    this.route('logout');
    this.route('edit-contact');
  });
});
