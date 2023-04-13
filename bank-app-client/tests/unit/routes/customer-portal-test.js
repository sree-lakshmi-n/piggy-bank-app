import { module, test } from 'qunit';
import { setupTest } from 'bank-app-client/tests/helpers';

module('Unit | Route | customer-portal', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:customer-portal');
    assert.ok(route);
  });
});
