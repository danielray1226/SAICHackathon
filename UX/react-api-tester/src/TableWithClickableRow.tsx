import { useState } from "react";

interface Props {
  name?: string;
  headers: string[]; // e.g. [ "header1", "header2"]
  rows: Object[]; // e.g. [ {"header1":"value1", "header2" : "value2"}, {"header1":"value3", "header2" : "value4"} ]
  onRowClicked?: (row: Object) => void;
}

function TableWithClickableRow({ name, headers, rows, onRowClicked }: Props) {
  const [selectedRowIndex, setSelectedRowIndex] = useState();

  function rowSelected(index: number) {
    //console.log(headers.length," Clicked on: " + JSON.stringify(rows[index]));
    setSelectedRowIndex(index);
    //if (rows.length==1)
    setTimeout(() => {
      setSelectedRowIndex(null);
    }, 200);
    if (onRowClicked) onRowClicked(rows[index]);
  }

  // https://getbootstrap.com/docs/4.0/content/tables/#contextual-classes

  return (
    <div className="table-responsive-sm">
      <table className="table table-hover table-striped table-bordered">
        <thead>
          {name && (
            <tr key={"name"} align="center">
              <th colSpan={headers.length}>{name}</th>
            </tr>
          )}

          {headers.length > 1 && (
            <tr key={"columns"}>
              {headers.map((h) => (
                <th key={h} scope="col">
                  {h}
                </th>
              ))}
            </tr>
          )}
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr
              key={rowIndex}
              className={selectedRowIndex === rowIndex ? "table-primary" : ""}
            >
              {headers.map((h, columnIndex) => (
                <td
                  key={rowIndex + "-" + columnIndex}
                  onClick={() => {
                    rowSelected(rowIndex);
                  }}
                >
                  {Array.isArray(row[h]) ||
                  (typeof row[h] === "object" && row[h] !== null)
                    ? JSON.stringify(row[h])
                    : "" + row[h]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
export default TableWithClickableRow;
