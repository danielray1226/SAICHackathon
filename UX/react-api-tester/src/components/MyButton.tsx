import { ReactNode } from "react";

interface Props {
  children: ReactNode;
  onClick: () => void;
  color?: "primary" | "secondary" | "danger";
  disabled?: boolean;
}

const MyButton = ({
  children,
  onClick,
  color = "primary",
  disabled = false,
}: Props) => {
  return (
    <button
      type="button"
      className={"btn " + "btn-" + color}
      onClick={() => {
        onClick();
      }}
      disabled={disabled}
    >
      {children}
    </button>
  );
};

export default MyButton;
