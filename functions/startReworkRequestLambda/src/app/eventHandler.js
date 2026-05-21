exports.handleEvent = async (event) => {
  console.log("Received event:", JSON.stringify(event));
  const { iun, attempt, recIndex, pcRetry, requestType} = event;

  if (requestType !== "RESTART") {
    console.log(`Request type '${requestType}' not handled. Skipping.`);
    return {
      handled: false,
      reason: `Request type '${requestType}' not handled`
    };
  }

  const payload = { attempt, recIndex, reason: "Mock restart request" };

  const response = await fetch(`${process.env.DELIVERYPUSH_BASEURL}/notifications/${iun}/restart-attempt`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });

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

  return {
    handled: true,
    requestType,
    response: responseBody
  };
};
