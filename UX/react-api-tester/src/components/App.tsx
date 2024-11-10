import ListGroup from "./components/ListGroup";
import Alert from "./components/Alert";
import MyButton from "./components/MyButton";
import { useState } from "react";
import FetchExample from "./components/FetchExample";
import LoginDialog from "./components/LoginDialog";
import { getLogoffUrl } from "./consts";

function plain() {
  console.log("hello");
}

function callLogoff(onLogoffCompleted: () => void) {
  console.log("LOGOFF");
  const url = getLogoffUrl();
  fetch(url, {
    method: "GET",
    credentials: "include",
  })
    .then((response) => {
      if (response.status === 200) console.log("Logged off indeed");
      else console.log("Hmmm didnt actuall logged off");
      onLogoffCompleted();
    })
    .catch((e) => {
      console.log(e);
      onLogoffCompleted();
    });
}

function App() {
  const [alerted, setAlerted] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [loginData, setLoginData] = useState({});
  const [logingOff, setLoginOff] = useState(false);

  console.log(JSON.stringify(loginData));

  const items1 = [
    "/",
    "/artifact-fields",
    "/artifact-fields/{field-id}/options",
    "/artifact-types",
    "/artifacts",
    "/artifacts/{id}",
    "/capabilities",
    "/projects",
  ];
  const items2 = ["d", "e", "f"];

  const handleItem = (h: string, id: number, item: string) => {
    console.log(h + " " + id + " : " + item);
  };

  const buttonClicked = () => {
    setAlerted(true);
  };
  const onAlertDismiss = () => {
    setAlerted(false);
  };

  const logoff = () => {
    setLoginOff(true);
    console.log("STARTED LOGIN OFF");
    callLogoff(() => {
      setLoginOff(false);
      setAuthenticated(false);
      console.log("FINISHED LOGIN OFF");
    });
    setAuthenticated(false);
  };

  if (logingOff) {
    return <p>Logging Off .... </p>;
  }

  if (!authenticated)
    return (
      <LoginDialog
        onLogin={(retrievedloginData) => {
          setAuthenticated(true);
          setLoginData(retrievedloginData);
          console.log("SAVED loginData: " + JSON.stringify(retrievedloginData));
        }}
      />
    );

  return (
    <>
      {console.log("APP IS RUNNING!!!!")}

      {alerted && (
        <Alert dismiss={onAlertDismiss}>
          hi there<p>blah</p>
        </Alert>
      )}
      <p>
        Hello, {loginData.name}
        <br />
      </p>
      <MyButton onClick={logoff}>
        Log off
        <br />
        <img src="data:image/png;base64,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" />
      </MyButton>

      <MyButton onClick={buttonClicked} disabled={alerted}>
        <p>Alert</p>
      </MyButton>

      <ListGroup
        items={items1}
        heading="OpenAPI Paths"
        onSelectItem={handleItem}
      />
      <ListGroup items={items2} heading="World" onSelectItem={handleItem} />

      <FetchExample key="myfetch" />
    </>
  );
}
export default App;
