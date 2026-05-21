const assert = require("assert");
const { handleEvent } = require("../app/eventHandler");

describe("handleEvent", () => {
  let originalFetch;
  let originalConsoleLog;
  let originalConsoleError;

  // Alias per compatibilita' con test() usato nel file originale
  const test = it;

  beforeEach(() => {
    process.env.DELIVERYPUSH_BASEURL = "https://delivery-push.test";

    originalFetch = global.fetch;
    originalConsoleLog = console.log;
    originalConsoleError = console.error;

    global.fetch = async () => {
      throw new Error("fetch stub not configured for this test");
    };

    console.log = () => {};
    console.error = () => {};
  });

  afterEach(() => {
    global.fetch = originalFetch;
    console.log = originalConsoleLog;
    console.error = originalConsoleError;
  });

  test("should skip event if requestType is not RESTART", async () => {
    let fetchCalled = false;
    global.fetch = async () => {
      fetchCalled = true;
      return {};
    };

    const event = {
      iun: "IUN123",
      attempt: 1,
      recIndex: 0,
      requestType: "OTHER"
    };

    const result = await handleEvent(event);

    assert.deepStrictEqual(result, {
      handled: false,
      reason: "Request type 'OTHER' not handled"
    });

    assert.strictEqual(fetchCalled, false);
  });

  test("should call restart-attempt API when requestType is RESTART", async () => {
    let calledUrl = null;
    let calledOptions = null;

    global.fetch = async (url, options) => {
      calledUrl = url;
      calledOptions = options;
      return {
        ok: true,
        json: async () => ({ result: "OK" })
      };
    };

    const event = {
      iun: "IUN123",
      attempt: 2,
      recIndex: 1,
      requestType: "RESTART"
    };

    const result = await handleEvent(event);

    assert.strictEqual(
      calledUrl,
      "https://delivery-push.test/notifications/IUN123/restart-attempt"
    );

    assert.deepStrictEqual(calledOptions, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        attempt: 2,
        recIndex: 1,
        reason: "Mock restart request"
      })
    });

    assert.deepStrictEqual(result, {
      handled: true,
      requestType: "RESTART",
      response: { result: "OK" }
    });
  });

  test("should throw error when REST call fails", async () => {
    global.fetch = async () => ({
      ok: false,
      status: 500,
      text: async () => "Internal error"
    });

    const event = {
      iun: "IUN123",
      attempt: 1,
      recIndex: 0,
      requestType: "RESTART"
    };

    await assert.rejects(
      async () => handleEvent(event),
      /REST call failed with status 500/
    );
  });

  test("should return null response if response body is not JSON", async () => {
    global.fetch = async () => ({
      ok: true,
      json: async () => {
        throw new Error("Invalid JSON");
      }
    });

    const event = {
      iun: "IUN123",
      attempt: 1,
      recIndex: 0,
      requestType: "RESTART"
    };

    const result = await handleEvent(event);

    assert.deepStrictEqual(result, {
      handled: true,
      requestType: "RESTART",
      response: null
    });
  });
});