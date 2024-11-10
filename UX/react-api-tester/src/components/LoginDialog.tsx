import { useEffect, useState } from "react";
import MyButton from "./MyButton";
import Alert from "./Alert";
import { getLoginUrl } from "../consts";

const style = {
  backgroundColor: "gray",
  color: "white",
  padding: "10px 20px",
  border: "none",
  borderRadius: "5px",
  //position: "absolute",
  top: "20%",
  left: "50%",
  marginTop: "10%",
  marginLeft: "30%",
  width: "300px",
};

interface Props {
  onLogin: (loginData: Object) => void;
}

export default function LoginDialog({ onLogin }: Props) {
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [alerted, setAlerted] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");

  function callAuth() {
    let body = JSON.stringify({
      login: login,
      password: password,
    });
    console.log("SUBMITTING " + body);
    const url = getLoginUrl();
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: new Headers({
        Accept: "application/json",
        "Content-Type": "application/json",
      }),
      body: body,
    })
      .then((response) => response.json())
      .then((data) => {
        console.log("FETCH GOT US: " + JSON.stringify(data));
        if (data.success) onLogin(data);
        else {
          setAlertMessage(data.message);
          setAlerted(true);
        }
      })
      .catch((e) => {
        console.log(e);
        setAlertMessage("Error connecting to " + url + " : " + e.message);
        setAlerted(true);
      });
  }

  useEffect(callAuth, []);

  return (
    <>
      {alerted && (
        <Alert
          dismiss={() => {
            setAlerted(false);
          }}
        >
          <p>{alertMessage}</p>
        </Alert>
      )}
      <div style={style}>
        <div className="mb-3">
          <label htmlFor="login" className="form-label">
            Login:
          </label>
          <input
            type="text"
            id="login"
            className="form-control"
            onChange={(e) => setLogin(e.target.value)}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="password">Password:</label>
          <input
            type="password"
            id="password"
            className="form-control"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") callAuth();
            }}
          />
        </div>
        <MyButton
          onClick={() => {
            console.log("CLICKED!!!" + new Date());
            //refresh();
            callAuth();
          }}
        >
          Login
        </MyButton>
      </div>
    </>
  );
}
