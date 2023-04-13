import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';

export default class NavbarComponent extends Component {
  @tracked options = this.args.model;
  @service('auth') auth;
  @service router;

  @action handleNavItemClick(event) {
    Array.from(event.target.closest('.nav-list').children).forEach((item) => {
      item.children[0].classList.remove('active-nav');
    });
    event.target.classList.add('active-nav');
    if (event.target.textContent == 'logout') {
      this.auth.logout();
    }
  }
}
