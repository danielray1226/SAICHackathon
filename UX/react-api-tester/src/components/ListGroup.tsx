import { useState } from "react";

interface Props {
  items: string[];
  heading: string;
  onSelectItem?: (heading: string, index: number, item: string) => void;
  defaultSelection?: number;
}

function ListGroup({ items, heading, onSelectItem, defaultSelection }: Props) {
  let i =
    defaultSelection === undefined
      ? -1
      : defaultSelection >= items.length
      ? 0
      : defaultSelection;
  // console.log(heading, "DEFAULT SELECTION: ", i);
  const [selectedIndex, setSelectedIndex] = useState(i);
  //  console.log(heading, " selectedIndex: ", selectedIndex);

  const mySelectedIndex = selectedIndex >= items.length ? i : selectedIndex;

  /*if (mySelectedIndex != selectedIndex)
    if (onSelectItem) {
      setTimeout(() => {
        onSelectItem(heading, mySelectedIndex, items[mySelectedIndex]);
      }, 0);
    }
      */

  return (
    <>
      <h1>{heading}</h1>
      {items.length === 0 && <p> no items</p>}
      <ul className="list-group">
        {items.map((item, index) => (
          <li
            key={heading + "-" + item + "-" + index}
            className={
              mySelectedIndex === index
                ? "list-group-item active"
                : "list-group-item"
            }
            onClick={() => {
              setSelectedIndex(index);
              if (onSelectItem != null && onSelectItem)
                onSelectItem(heading, index, item);
            }}
          >
            {item}
          </li>
        ))}
      </ul>
    </>
  );
}
export default ListGroup;
