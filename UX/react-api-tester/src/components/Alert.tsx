import { ReactNode } from "react";

interface Props {
  children: ReactNode;
  dismiss: () => void;
}
const Alert = ({ children, dismiss }: Props) => {
  return (
    <div
      className="alert alert-primary alert-dismissible fade show"
      role="alert"
    >
      {children}
      <button
        type="button"
        className="btn-close"
        data-bs-dismiss="alert"
        aria-label="Close"
        onClick={dismiss === null ? () => {} : dismiss}
      ></button>
    </div>
  );
};

export default Alert;
