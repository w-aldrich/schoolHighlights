package myServer;

@SuppressWarnings("serial")
public class BadRequestException extends Exception
{	
	public BadRequestException()
	{
		super();
	}
	
	public BadRequestException(String s)
	{
		super(s);
	}
	
	public BadRequestException(Throwable e)
	{
		super(e);
	}
}
