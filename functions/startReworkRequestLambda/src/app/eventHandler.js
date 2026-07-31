exports.handleEvent = async (event) => {
  console.log("Received event:", JSON.stringify(event));

  const results = [];

  for (const record of event.Records ?? []) {
    const body = JSON.parse(record.body);

    const { iun, attemptId, recIndex, pcRetry, requestType } = body;

    if (requestType !== "RESTART") {
      console.log(`Request type '${requestType}' not handled. Skipping.`);

      results.push({
        handled: false,
        reason: `Request type '${requestType}' not handled`
      });

      continue;
    }

    console.log("Processing RESTART request", {
      iun,
      attemptId,
      recIndex,
      pcRetry
    });

    const payload = {
      attemptId,
      recIndex,
      reason: "Mock restart request"
    };

    const response = await fetch(
      `${process.env.DELIVERYPUSH_BASEURL}/notifications/${iun}/restart-attempt`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      }
    );

    if (!response.ok) {
      const responseBody = await response.text();

      console.error("REST call failed:", {
        status: response.status,
        body: responseBody
      });

      throw new Error(`REST call failed with status ${response.status}`);
    }

    const responseBody = await response.json().catch(() => null);

    console.log("REST call completed successfully:", responseBody);

    results.push({
      handled: true,
      requestType,
      response: responseBody
    });
  }

  return {
    results
  };
};