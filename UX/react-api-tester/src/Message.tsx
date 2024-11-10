
function Message()  {
    const name='';
    if (name)
      return <div>Hi {name}</div>;
    else return <div>Hi there</div>;
}
export default Message;