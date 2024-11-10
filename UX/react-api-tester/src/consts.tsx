export function getLoginUrl() {
  console.log("MODE: " + import.meta.env.MODE);
  console.log("BASE_URL: " + import.meta.env.BASE_URL);
  console.log("DEV: " + import.meta.env.DEV);
  console.log("PROD: " + import.meta.env.PROD);
  if (import.meta.env.MODE != "development") return "/login";
  return "http://localhost:8000/login";
}
export function getAPICallerUrl() {
  if (import.meta.env.MODE != "development") return "/APICaller";
  return "http://localhost:8000/APICaller";
}

export function getOpenAPIDataUrl() {
  if (import.meta.env.MODE != "development") return "/OpenApiData";
  return "http://localhost:8000/OpenApiData";
}

export function getLogoffUrl() {
  if (import.meta.env.MODE != "development") return "/logoff";
  return "http://localhost:8000/logoff";
}

export function isDev() {
  return import.meta.env.MODE == "development";
}

/*
  we can save openApi json object here, to avoid passing it through the props
*/
let openApiObject = null;
export function getOpenApi() {
  return openApiObject;
}

export function loadOpenApi(
  onData?: (openApi: Object) => void,
  onError?: (ex) => void
) {
  const url = getOpenAPIDataUrl();
  console.log("Calling: ", url);
  fetch(url, {
    method: "GET",
    credentials: "include",
    headers: new Headers({
      Accept: "application/json",
      "Content-Type": "application/json",
    }),
  })
    .then((response) => response.json())
    .then((data) => {
      openApiObject = data;
      if (onData) onData(data);
    })
    .catch((e) => {
      if (onError) onError(e);
    });
}

export function callApi(
  onData?: (data: Object) => void,
  onError?: (ex) => void,
  serverUrl: string,
  path: string,
  method: string,
  paramValues: Object
) {
  /*
			tomcat will expect following json object, e.g.:
			{
				"path" : "/artifact-fields/{field-id}/options",
				"method" : "get",
				"parameters" : [
					{
						"name": "field-id",
						"value" : 123
					}, 
					{
						"name": "project",
						"value": "FooBar"
					}
				],
				"body" : "POST body as a string or json"
			}
  */
  let body = {
    serverUrl: serverUrl,
    path: path,
    method: method,
    parameters: [],
  };
  for (const [key, value] of Object.entries(paramValues)) {
    body.parameters.push({ name: key, value: value });
  }
  const b = JSON.stringify(body);
  const url = getAPICallerUrl();
  console.log("Calling: ", url, ", with payload: ", b);
  fetch(url, {
    method: "POST",
    credentials: "include",
    headers: new Headers({
      Accept: "application/json",
      "Content-Type": "application/json",
    }),
    body: b,
  })
    .then((response) => response.json())
    .then((data) => {
      if (onData) onData(data);
    })
    .catch((e) => {
      console.log(e);
      if (onError) onError(e);
    });
}

export function getJsonLeaf(obj: Object, path: string[]) {
  let currentObject = obj;
  for (let index = 0; index < path.length; index++) {
    const nameOfIndex = path[index];
    let newObj = currentObject[nameOfIndex];
    if (!newObj) {
      return null;
    }
    currentObject = newObj;
  }
  return currentObject;
}

export function getOpenApiSchemaByRef(ref: string) {
  const reference = ref.substring(2); // we assume ref is like "#/components/schemas/PagedOptions"
  const splitted = reference.split("/");
  //console.log("Reference: ", reference);
  //console.log("Stringified: ", JSON.stringify(splitted));
  return getJsonLeaf(getOpenApi(), splitted); // e.g. json object representing: components->schemas->Artifact
}

export function getOpenApiSchemaPropertiesByRef(ref: string) {
  const refDef = getOpenApiSchemaByRef(ref); // e.g. json object representing: components->schemas->Artifact
  if (refDef) return refDef["properties"];
  return null;
}

/*
for arguments ("/projects", "get", "200") will return "#/components/schemas/PagedOptions"
*/
export function getOpenApiResponseSchemaRef(
  path: string,
  httpMethod: string,
  statusCode: number
) {
  //console.log(getJsonLeaf(getOpenApi(), ["paths", path, httpMethod]));
  const content = getJsonLeaf(getOpenApi(), [
    "paths",
    path,
    httpMethod,
    "responses",
    "" + statusCode,
    "content",
  ]); // "paths"->"/projects"->"get"->"responses"->"200"->"content"

  //console.log("Content: ", content);

  if (!content) return null;
  let schema;
  for (const [mime, value] of Object.entries(content)) {
    // we assume only one response mime type, e.g. "application/json" : { "schema" : {"$ref":...}}
    console.log("extracting first response mime type:", `${mime}: ${value}`);
    schema = value["schema"];
    if (schema && schema["$ref"]) return schema["$ref"];
  }
  return null;
}

function isPropertyDefTypeOf(propertyDef: Object, expectedType: string) {
  const type = propertyDef["type"];
  if (Array.isArray(type)) {
    for (const element of type) {
      if (element == expectedType) return true;
    }
  } else {
    return expectedType == type;
  }
}

export function getResultHeadersAndRows(
  property: string,
  propertyDef: Object,
  propertyData: Object
) {
  let headers = [];
  let rows = [];

  /* 
  lets see, if propertyDef refers to a yet another schema object.
  we dont really know what are the rules here, assume, that we only need to cover use case
    {
      "description": "The items for this page",
      "items": {
          "$ref": "#/components/schemas/Option"
      },
      "type": "array"
    }
  */
  if (
    // check, if its an array
    isPropertyDefTypeOf(propertyDef, "array") &&
    Array.isArray(propertyData)
  ) {
    //
    console.log("both property type, and the data are arrays");
    if (propertyDef["items"] && propertyDef["items"]["$ref"]) {
      const rowRef = propertyDef["items"]["$ref"];
      const actualProperties = getOpenApiSchemaPropertiesByRef(rowRef);
      console.log("actualProperties: ", JSON.stringify(actualProperties));
      headers = Object.keys(actualProperties); // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/keys
      rows = propertyData;
      console.log(
        "\n\n\n\n Property: ",
        property,
        "\n\n\n\n Propety Def: ",
        JSON.stringify(propertyDef),
        "\n\n\n\n Property Data: ",
        JSON.stringify(propertyData),
        "\n\n Headers: ",
        JSON.stringify(headers),
        "\n\n Rows: ",
        JSON.stringify(rows)
      );
      return [headers, rows];
    }
  }

  if (
    isPropertyDefTypeOf(propertyDef, "object") &&
    typeof propertyData === "object" &&
    propertyData !== null
  ) {
    // it looks like the data is an object, and the type is an object
    /*
    case for:
                      "attributes": {
                        "additionalProperties": {
                            
                        },
                        "description": "Map of field IDs to their corresponding values.",
                        "example": {
                            "formatted_id": "US123",
                            "id": "1234567",
                            "revision": "a3f67e2b5f1b5fda5dee3225b644525f",
                            "summary": "Lorem ipsum"
                        },
                        "type": "object"
                    },
    */
    headers = Object.keys(propertyData);
    rows = [propertyData];
    return [headers, rows];
  }

  if (propertyData) {
    /*
    case for:
                        "next_cursor": {
                        "description": "The cursor for the next page, or not provided if the last element is included in the payload",
                        "type": [
                            "string",
                            "null"
                        ]
                    }
    */
    headers = [property];
    let valObj = {};
    valObj[property] = propertyData;
    rows = [valObj];
  }

  console.log(
    "Extracting headers/rows for property: ",
    property,
    ", propertyDef" + JSON.stringify(propertyDef),
    ", and propertyData:",
    JSON.stringify(propertyData)
  );

  return [headers, rows];
}
