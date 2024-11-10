import { getResultHeadersAndRows } from "./consts";
import TableWithClickableRow from "./TableWithClickableRow";

interface Props {
  property: string;
  def: Object;
  data: Object;
  onRowClicked?: (row: Object) => void;
}

export default function RenderResults({
  property,
  def,
  data,
  onRowClicked,
}: Props) {
  const [headers, rows] = getResultHeadersAndRows(property, def, data);

  return (
    <>
      <TableWithClickableRow
        name={property}
        headers={headers}
        rows={rows}
        onRowClicked={onRowClicked}
      />
    </>
  );
  /*
        <TableWithClickableRow
        name="next_cursor"
        headers={["next_cursor"]}
        rows={[{ next_cursor: "bzzzz" }]}
        onRowClicked={onData}
      />

  return (
    <div>
      Property: {property}
      <br />
      <br />
      Def: {JSON.stringify(def)}
      <br />
      <br />
      Data: {JSON.stringify(data)}
      <br />
      <br />
    </div>
  );*/
}
