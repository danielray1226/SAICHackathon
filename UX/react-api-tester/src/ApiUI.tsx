import ListGroup from "./components/ListGroup";
import MyButton from "./components/MyButton";
import { callApi, loadOpenApi } from "./consts";
import { useEffect, useState } from "react";
import Parameters from "./Parameters";
import ServerResults from "./ServerResults";

function ApiUI() {
  const [openApi, setOpenApi] = useState();
  const [openApiError, setOpenApiError] = useState();
  const [currentPath, setPath] = useState();
  const [httpMethod, setHttpMethod] = useState();
  const [apiServerUrl, setApiServerUrl] = useState();
  const [paramsValid, setParamsValid] = useState(true);
  const [parameters, setParameters] = useState({});
  const [serverCallResult, setServerCallResult] = useState();
  const [serverCallParameters, setServerCallParameters] = useState();
  const [serverCallError, setServerCallError] = useState();
  const [serverIsCalled, setServerIsCalled] = useState(false);

  const [advisoryParameters, setAdvisoryParameters] = useState({});

  function onPathSelected(heading: string, i: number, path: string) {
    setPath(path);
    setHttpMethod(null);
    setParameters({});
    //setServerCallResult(null);
    setServerCallError(null);
    setServerIsCalled(false);
  }
  function onHttpMethodSelected(heading: string, i: number, method: string) {
    console.log("METHOD SELECTED!!!!", method);
    setHttpMethod(method);
    setParameters({});
    setServerIsCalled(false);
    setServerIsCalled(false);
    setServerCallError(null);
    //setServerCallResult(null);
  }
  function onServerUrlSelected(heading: string, i: number, url: string) {
    setApiServerUrl(url);
    setServerIsCalled(false);
  }

  function onOpenApiLoaded(data: Object) {
    setOpenApi(data);
  }
  function onOpenApiError(ex: any) {
    setOpenApiError(ex);
  }
  function onParamsChange(valid: boolean, parameters: Object) {
    //console.log("On Parameter Change: is valid: ", valid, ", got parameters as: ", parameters)
    setParamsValid(valid);
    setParameters(parameters);
  }

  function onResultRowClicked(path: string, property: string, row: Object) {
    console.log("row is ", JSON.stringify(row), " Path: ", path);
    setAdvisoryParameters((prev) => {
      let advise = { ...prev };
      for (const [key, value] of Object.entries(row)) {
        if (path == "/projects" && key == "id") {
          advise["project"] = value;
        } else if (key == "next_cursor") {
          advise["cursor"] = value;
        } else if (path == "/artifact-types" && key == "id") {
          advise["type"] = value;
        } else {
          advise[key] = value;
        }
      }
      console.log(
        "\n\n\n\nAdded advisory parameters: ",
        JSON.stringify(advise),
        "\n\n\n\n"
      );
      return advise;
    });
  }

  useEffect(() => {
    loadOpenApi(onOpenApiLoaded, onOpenApiError);
  }, []);

  console.log("Error: ", openApiError);

  if (openApiError)
    return <p>Error loading open api: {openApiError.message}</p>;
  if (!openApi) return <p>Loading OpenAPI definitions ...</p>;

  // we got open api, lets find out all the paths
  const pathsObject = openApi["paths"];
  const pathNames = Object.keys(pathsObject);
  if (!pathNames || pathNames.length === 0)
    return <p>Error: OpenAPI did not provide any paths </p>;

  console.log("PATH NAMES: ", pathNames);

  let myPath = currentPath ? currentPath : pathNames[0];
  const pathObject = pathsObject[myPath]; // Object which has get/post.. properties
  // lets find out, what are the http methods
  const httpMethods = Object.keys(pathObject);
  console.log("HTTP METHODS: ", httpMethods);
  let myHttpMethod = httpMethod ? httpMethod : httpMethods[0];

  const methodObject = pathObject[myHttpMethod];
  const methodObjectDescription = methodObject["description"];
  const methodObjectParameters = methodObject["parameters"];
  console.log("Description: ", methodObjectDescription);
  console.log("Parameter Definitions: ", methodObjectParameters);

  console.log("WILL USE HTTP METHOD: ", myHttpMethod);

  let serversArray = openApi["servers"];
  let serverUrls = [];
  for (const s of serversArray) {
    serverUrls.push(s["url"]);
  }
  console.log("SERVER URLS: ", serverUrls);
  const myServerUrl = apiServerUrl
    ? apiServerUrl
    : serverUrls.length > 0
    ? serverUrls[0]
    : null;
  console.log("Will use url: " + myServerUrl);

  function onApiServerCallResult(data: Object) {
    setServerCallResult(data);
    setServerIsCalled(false);
    setServerCallError(null);
  }
  function onApiServerCallError(ex) {
    setServerCallResult(null);
    setServerIsCalled(false);
    setServerCallError(ex);
  }

  function callServerUrl() {
    setServerIsCalled(true);
    setServerCallError(null);
    setServerCallResult(null);
    // lets save server call parameters
    const callParams = {
      url: myServerUrl,
      path: myPath,
      httpMethod: myHttpMethod,
      parameters: parameters,
    };
    setServerCallParameters(callParams);
    //setServerCallParameters(JSON.stringify(callParams));

    callApi(
      onApiServerCallResult /* onData?: (data: Object) => void,*/,
      onApiServerCallError, //onError?: (ex) => void,
      myServerUrl,
      myPath,
      myHttpMethod,
      parameters
    );
  }

  return (
    <>
      <div className="container">
        <ListGroup
          items={pathNames}
          onSelectItem={onPathSelected}
          heading="OpenAPI Paths"
          defaultSelection={0}
        />
      </div>
      {httpMethods.length > 0 && (
        <>
          <br />
          <div className="container">
            <ListGroup
              key={"http_method_for-" + myPath}
              items={httpMethods}
              onSelectItem={onHttpMethodSelected}
              heading="HTTP Method"
              defaultSelection={0}
            />
            <>{methodObjectDescription}</>
          </div>
        </>
      )}
      {serverUrls.length > 0 && (
        <>
          {methodObjectParameters && (
            <>
              <br />
              <div className="container">
                <Parameters
                  key={"params-" + myPath + "-" + myHttpMethod}
                  params={methodObjectParameters}
                  onParamsChange={onParamsChange}
                  advisoryParameters={advisoryParameters}
                />
              </div>
            </>
          )}
          <div className="container">
            <ListGroup
              items={serverUrls}
              onSelectItem={onServerUrlSelected}
              heading="Server URLs"
              defaultSelection={0}
            />
          </div>
          {myHttpMethod &&
            (methodObjectParameters === undefined || paramsValid) && (
              <>
                <br />
                <div className="container">
                  <MyButton
                    color="danger"
                    onClick={callServerUrl}
                    disabled={serverIsCalled}
                  >
                    Call {myServerUrl}
                  </MyButton>
                </div>
              </>
            )}
        </>
      )}
      {serverCallParameters && (
        <>
          <br />
          <div className="container">
            <ul className="list-group">
              <li className="list-group-item">URL: {myServerUrl}</li>
              <li className="list-group-item">Path: {myPath}</li>
              <li className="list-group-item">Method: {myHttpMethod}</li>
              <li className="list-group-item">
                With parameters {JSON.stringify(parameters)}
              </li>
            </ul>
          </div>
          <br />
          <div className="container">
            {/*<Component to display neatly =openAPI =severCallResult/>*/}
            {serverCallResult && serverCallParameters.path == myPath && (
              <>
                <ServerResults
                  key={JSON.stringify(serverCallParameters)}
                  result={serverCallResult}
                  path={serverCallParameters.path}
                  httpMethod={serverCallParameters.httpMethod}
                  onRowClicked={onResultRowClicked}
                />
                <br />
                <h2>Raw Results</h2>
                {JSON.stringify(serverCallResult)}
              </>
            )}
          </div>

          <div className="container"></div>
        </>
      )}

      {serverCallError && (
        <>
          <br />
          <div className="container">{serverCallError.message}</div>
        </>
      )}
    </>
  );
}
//<PathSelector path={myPath} />
/*
    const callParams = {
      url: myServerUrl,
      path: myPath,
      httpMethod: myHttpMethod,
      parameters: parameters,
    };
*/
export default ApiUI;
