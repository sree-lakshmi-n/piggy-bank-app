import { module, test } from 'qunit';
import { setupTest } from 'bank-app-client/tests/helpers';

module('Unit | Controller | customer-portal/transfer', function (hooks) {
  setupTest(hooks);

  // TODO: Replace this with your real tests.
  test('it exists', function (assert) {
    let controller = this.owner.lookup('controller:customer-portal/transfer');
    assert.ok(controller);
  });
});
