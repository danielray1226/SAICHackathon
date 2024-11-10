import { useEffect, useState } from "react";
import RenderResults from "./RenderResults";
import {
  getOpenApiResponseSchemaRef,
  getOpenApiSchemaPropertiesByRef,
} from "./consts";

interface Props {
  result: Object;
  path: string;
  httpMethod: string;
  onRowClicked?: (path: string, property: string, row: Object) => void;
}

export default function ServerResults({
  result,
  path,
  httpMethod,
  onRowClicked,
}: Props) {
  if (!result || !result["data"]) return <></>;

  function onData(property: string, data: Object) {
    if (onRowClicked) onRowClicked(path, property, data);
  }

  const ref = getOpenApiResponseSchemaRef(
    path,
    httpMethod,
    result["statusCode"]
  );

  console.log("Got ref: ", ref, "for ", path, httpMethod, result["statusCode"]);

  let resultData = result["data"];
  //resultData["next_cursor"] = "zzzz";

  console.log("Need to render: ", JSON.stringify(resultData));

  if (!ref || !resultData) return <></>;

  const properties = getOpenApiSchemaPropertiesByRef(ref);
  console.log("Properties: ", JSON.stringify(properties));
  console.log("\n\n\n Object Keys: ", Object.keys(properties));
  return (
    <>
      <div>
        {Object.keys(properties).map((key) => (
          <RenderResults
            key={key}
            property={key}
            def={properties[key]}
            data={resultData[key]}
            onRowClicked={(data: Object) => onData(key, data)}
          />
        ))}
      </div>
    </>
  );
}
