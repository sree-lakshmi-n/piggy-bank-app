import Component from '@glimmer/component';
import { inject as service } from '@ember/service';

export default class TransactionsComponent extends Component {
  @service('auth') auth;
}
