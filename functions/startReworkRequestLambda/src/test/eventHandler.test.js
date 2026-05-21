const { handleEvent } = require("./index");

describe("handleEvent", () => {
  beforeEach(() => {
    process.env.DELIVERYPUSH_BASEURL = "https://delivery-push.test";
    global.fetch = jest.fn();
    jest.spyOn(console, "log").mockImplementation(() => {});
    jest.spyOn(console, "error").mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  test("should skip event if requestType is not RESTART", async () => {
    const event = {
      iun: "IUN123",
      attempt: 1,
      recIndex: 0,
      requestType: "OTHER"
    };

    const result = await handleEvent(event);

    expect(result).toEqual({
      handled: false,
      reason: "Request type 'OTHER' not handled"
    });

    expect(fetch).not.toHaveBeenCalled();
  });

  test("should call restart-attempt API when requestType is RESTART", async () => {
    fetch.mockResolvedValue({
      ok: true,
      json: jest.fn().mockResolvedValue({ result: "OK" })
    });

    const event = {
      iun: "IUN123",
      attempt: 2,
      recIndex: 1,
      requestType: "RESTART"
    };

    const result = await handleEvent(event);

    expect(fetch).toHaveBeenCalledWith(
      "https://delivery-push.test/notifications/IUN123/restart-attempt",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          attempt: 2,
          recIndex: 1,
          reason: "Mock restart request"
        })
      }
    );

    expect(result).toEqual({
      handled: true,
      requestType: "RESTART",
      response: { result: "OK" }
    });
  });

  test("should throw error when REST call fails", async () => {
    fetch.mockResolvedValue({
      ok: false,
      status: 500,
      text: jest.fn().mockResolvedValue("Internal error")
    });

    const event = {
      iun: "IUN123",
      attempt: 1,
      recIndex: 0,
      requestType: "RESTART"
    };

    await expect(handleEvent(event)).rejects.toThrow(
      "REST call failed with status 500"
    );
  });

  test("should return null response if response body is not JSON", async () => {
    fetch.mockResolvedValue({
      ok: true,
      json: jest.fn().mockRejectedValue(new Error("Invalid JSON"))
    });

    const event = {
      iun: "IUN123",
      attempt: 1,
      recIndex: 0,
      requestType: "RESTART"
    };

    const result = await handleEvent(event);

    expect(result).toEqual({
      handled: true,
      requestType: "RESTART",
      response: null
    });
  });
});