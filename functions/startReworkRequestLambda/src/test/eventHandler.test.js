const assert = require("assert");
const { handleEvent } = require("../app/eventHandler");

describe("handleEvent", () => {
  let originalFetch;
  let originalConsoleLog;
  let originalConsoleError;

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
      attemptId: 1,
      recIndex: 0,
      requestType: "OTHER"
    };

    const result = await handleEvent({
      Records: [
        {
          body: JSON.stringify(event)
        }
      ]
    });

    assert.deepStrictEqual(result, {
      results: [
        {
          handled: false,
          reason: "Request type 'OTHER' not handled"
        }
      ]
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
      attemptId: 2,
      recIndex: 1,
      requestType: "RESTART"
    };

    const result = await handleEvent({
      Records: [
        {
          body: JSON.stringify(event)
        }
      ]
    });

    assert.strictEqual(
      calledUrl,
      "https://delivery-push.test/notifications/IUN123/restart-attempt"
    );

    assert.deepStrictEqual(calledOptions, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        attemptId: 2,
        recIndex: 1,
        reason: "Mock restart request"
      })
    });

    assert.deepStrictEqual(result, {
      results: [
        {
          handled: true,
          requestType: "RESTART",
          response: { result: "OK" }
        }
      ]
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
      attemptId: 1,
      recIndex: 0,
      requestType: "RESTART"
    };

    await assert.rejects(
      async () =>
        handleEvent({
          Records: [
            {
              body: JSON.stringify(event)
            }
          ]
        }),
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
      attemptId: 1,
      recIndex: 0,
      requestType: "RESTART"
    };

    const result = await handleEvent({
      Records: [
        {
          body: JSON.stringify(event)
        }
      ]
    });

    assert.deepStrictEqual(result, {
      results: [
        {
          handled: true,
          requestType: "RESTART",
          response: null
        }
      ]
    });
  });
});