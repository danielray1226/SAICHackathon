import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "bootstrap/dist/css/bootstrap.css";
import Playground from "./Playground.tsx";

createRoot(document.getElementById("root")!).render(<App />);
//createRoot(document.getElementById("playground")!).render(<Playground />);
