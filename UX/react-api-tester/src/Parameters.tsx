import { useState } from "react";

interface ParamProps {
  params: Array;
  onParamsChange?: (isValid: boolean, parameters: Object) => void;
  advisoryParameters: Object;
}

function calculateAllValues(params: Array, advisoryParameters: Object) {
  let ret = {};
  for (let param of params) {
    const name = param["name"];
    let av = advisoryParameters[name];
    if (av) ret[name] = av;
  }
  /*console.log(
    "\n\n\nCALCULATED: ",
    ret,
    " for ",
    JSON.stringify(params),
    " and ",
    JSON.stringify(advisoryParameters),
    "\n\n\n"
  );*/
  return ret;
}

function Parameters({
  params,
  onParamsChange,
  advisoryParameters,
}: ParamProps) {
  console.log(params);
  const [allValues, setAllValues] = useState(
    //{}
    calculateAllValues(params, advisoryParameters)
  );
  if (!params) return <></>;

  /*console.log(
    "RENDER ADVISORY PARAMETERS: " + JSON.stringify(advisoryParameters)
  );
*/
  function classStyle(i: number) {
    const param = params[i];
    const required = param["required"];
    const name = param["name"];
    console.log(
      "calculating style for index ",
      i,
      ", parameter ",
      params[i],
      ", required param ",
      param.required
    );
    const currentValue = allValues[name];
    var style = "form-control";
    if (!currentValue && required) style += " is-invalid";
    return style;
  }
  function isValid() {
    for (const param of params) {
      const required = param["required"];
      const name = param["name"];
      const currentValue = allValues[name];
      if (!currentValue && required) return false;
    }
    return true;
  }
  function onChange(param: Object, value: string) {
    if (param["name"]) {
      const extra = {};
      extra[param.name] = value;
      const newAllVals = { ...allValues, ...extra };
      //console.log("Params value: " + newAllVals);
      setAllValues(newAllVals);
      if (onParamsChange) {
        onParamsChange(isValid(), newAllVals);
      }
    }
  }
  setTimeout(() => {
    if (onParamsChange) {
      //console.log("CALLING INITIAL PARAMS VALID: ", isValid());
      onParamsChange(isValid(), allValues);
    }
  }, 0);
  // https://getbootstrap.com/docs/5.0/forms/floating-labels/
  //console.log("param is: ", JSON.stringify(params));
  return (
    <>
      <h1>Parameters</h1>
      <ul className="list-group">
        {params.map((param, index) => (
          <>
            <label htmlFor={"param" + index}>{param["name"]}</label>
            <input
              type="text"
              className={classStyle(index)}
              id={"param" + index}
              key={"param" + index}
              value={allValues[param["name"]]}
              onChange={(e) => {
                onChange(param, e.target.value);
              }}
            />
          </>
        ))}
      </ul>
    </>
  );
  return <label>Parameters</label>;
}
export default Parameters;
